package pl.edu.agh.gem.internal.service

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.group.createGroupMembers
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.client.CurrencyManagerClient
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.model.currency.Currencies
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.util.DummyData.CURRENCY_1
import pl.edu.agh.gem.util.DummyData.CURRENCY_2
import pl.edu.agh.gem.util.createCurrencies
import pl.edu.agh.gem.util.createExchangeRate
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createExpenseParticipant
import pl.edu.agh.gem.util.createGroupOptions
import pl.edu.agh.gem.validator.ValidatorsException
import java.math.BigDecimal
import java.time.Instant

class ExpenseServiceTest : ShouldSpec({
    val groupManagerClient = mock<GroupManagerClient> { }
    val currencyManagerClient = mock<CurrencyManagerClient> {}
    val expenseRepository = mock<ExpenseRepository> {}
    val expenseService = ExpenseService(
        groupManagerClient = groupManagerClient,
        currencyManagerClient = currencyManagerClient,
        expenseRepository = expenseRepository,
    )
    should("get group members from client") {
        // given
        val groupMembers = createGroupMembers(listOf(USER_ID, OTHER_USER_ID))
        whenever(groupManagerClient.getGroupMembers(GROUP_ID)).thenReturn(groupMembers)

        // when
        val result = expenseService.getGroupMembers(GROUP_ID)

        // then
        verify(groupManagerClient, atLeastOnce()).getGroupMembers(GROUP_ID)
        result shouldBe groupMembers
    }

    should("create expense") {
        // given
        val expense = createExpense()
        val groupMembers = createGroupMembers(listOf(USER_ID, OTHER_USER_ID))
        val exchangeRate = createExchangeRate()
        val expected = expense.copy(exchangeRate = exchangeRate)
        whenever(groupManagerClient.getGroupOptions(GROUP_ID)).thenReturn(createGroupOptions(currencies = createCurrencies(CURRENCY_1, CURRENCY_2)))
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(exchangeRate)
        whenever(expenseRepository.create(anyVararg(Expense::class))).thenReturn(expected)

        // when
        val result = expenseService.create(groupMembers, expense)

        // then
        result shouldNotBe null
        result.also {
            it.id shouldBe expected.id
            it.groupId shouldBe expected.groupId
            it.creatorId shouldBe expected.creatorId
            it.cost shouldBe expected.cost
            it.baseCurrency shouldBe expected.baseCurrency
            it.targetCurrency shouldBe expected.targetCurrency
            it.exchangeRate shouldBe expected.exchangeRate
            it.createdAt shouldBe expected.createdAt
            it.updatedAt shouldBe expected.updatedAt
            it.expenseDate shouldBe expected.expenseDate
            it.attachmentId shouldBe expected.attachmentId
            it.expenseParticipants shouldHaveSize expected.expenseParticipants.size
            it.expenseParticipants.first().also { participant ->
                participant.participantId shouldBe expected.expenseParticipants.first().participantId
                participant.participantCost shouldBe expected.expenseParticipants.first().participantCost
                participant.participantStatus shouldBe expected.expenseParticipants.first().participantStatus
            }
            it.expenseParticipants.last().also { participant ->
                participant.participantId shouldBe expected.expenseParticipants.last().participantId
                participant.participantCost shouldBe expected.expenseParticipants.last().participantCost
                participant.participantStatus shouldBe expected.expenseParticipants.last().participantStatus
            }
            it.status shouldBe expected.status
            it.statusHistory shouldHaveSize expected.statusHistory.size
            it.statusHistory.first().also { entry ->
                entry.createdAt shouldBe expected.statusHistory.first().createdAt
                entry.expenseAction shouldBe expected.statusHistory.first().expenseAction
                entry.participantId shouldBe expected.statusHistory.first().participantId
                entry.comment shouldBe expense.statusHistory.first().comment
            }
            it.statusHistory.last().also { entry ->
                entry.createdAt shouldBe expected.statusHistory.last().createdAt
                entry.expenseAction shouldBe expected.statusHistory.last().expenseAction
                entry.participantId shouldBe expected.statusHistory.last().participantId
                entry.comment shouldBe expense.statusHistory.last().comment
            }
        }
        verify(groupManagerClient, times(1)).getGroupOptions(GROUP_ID)
        verify(currencyManagerClient, times(1)).getAvailableCurrencies()
        verify(currencyManagerClient, times(1)).getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))
        verify(expenseRepository, times(1)).create(anyVararg(Expense::class))
    }

    should("throw ValidatorsException when participants costs do not sum up to full cost") {
        // given
        val expense = createExpense(cost = BigDecimal.TEN)
        val groupMembers = createGroupMembers(listOf(USER_ID, OTHER_USER_ID))
        whenever(groupManagerClient.getGroupOptions(GROUP_ID)).thenReturn(createGroupOptions(currencies = createCurrencies(CURRENCY_1, CURRENCY_2)))
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(createExchangeRate())

        // when & then

        shouldThrowExactly<ValidatorsException> { expenseService.create(groupMembers, expense) }
        verify(expenseRepository, times(0)).create(anyVararg(Expense::class))
    }

    should("throw ValidatorsException when user is not in expense participants") {
        // given
        val expense = createExpense(creatorId = "nonGroupMember")
        val groupMembers = createGroupMembers(listOf(USER_ID, OTHER_USER_ID))
        whenever(groupManagerClient.getGroupOptions(GROUP_ID)).thenReturn(createGroupOptions(currencies = createCurrencies(CURRENCY_1, CURRENCY_2)))
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(createExchangeRate())

        // when & then

        shouldThrowExactly<ValidatorsException> { expenseService.create(groupMembers, expense) }
        verify(expenseRepository, times(0)).create(anyVararg(Expense::class))
    }

    should("throw ValidatorsException when participants ids are not unique") {
        // given
        val expense = createExpense(expenseParticipants = listOf(createExpenseParticipant(), createExpenseParticipant()))
        val groupMembers = createGroupMembers(listOf(USER_ID, OTHER_USER_ID))
        whenever(groupManagerClient.getGroupOptions(GROUP_ID)).thenReturn(createGroupOptions(currencies = createCurrencies(CURRENCY_1, CURRENCY_2)))
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(createExchangeRate())

        // when & then

        shouldThrowExactly<ValidatorsException> { expenseService.create(groupMembers, expense) }
        verify(expenseRepository, times(0)).create(anyVararg(Expense::class))
    }

    should("throw ValidatorsException when participants size is to low") {
        // given
        val expense = createExpense(expenseParticipants = listOf(createExpenseParticipant()))
        val groupMembers = createGroupMembers(listOf(USER_ID, OTHER_USER_ID))
        whenever(groupManagerClient.getGroupOptions(GROUP_ID)).thenReturn(createGroupOptions(currencies = createCurrencies(CURRENCY_1, CURRENCY_2)))
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(createExchangeRate())

        // when & then

        shouldThrowExactly<ValidatorsException> { expenseService.create(groupMembers, expense) }
        verify(expenseRepository, times(0)).create(anyVararg(Expense::class))
    }

    should("throw ValidatorsException when not all participants are group members") {
        // given
        val expense = createExpense(expenseParticipants = listOf(createExpenseParticipant("notGroupMember")))
        val groupMembers = createGroupMembers(listOf(USER_ID, OTHER_USER_ID))
        whenever(groupManagerClient.getGroupOptions(GROUP_ID)).thenReturn(createGroupOptions(currencies = createCurrencies(CURRENCY_1, CURRENCY_2)))
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(createExchangeRate())

        // when & then

        shouldThrowExactly<ValidatorsException> { expenseService.create(groupMembers, expense) }
        verify(expenseRepository, times(0)).create(anyVararg(Expense::class))
    }

    should("throw ValidatorsException when base currency is not among group currencies") {
        // given
        val expense = createExpense()
        val groupMembers = createGroupMembers(listOf(USER_ID, OTHER_USER_ID))
        whenever(groupManagerClient.getGroupOptions(GROUP_ID)).thenReturn(createGroupOptions(currencies = Currencies(listOf())))
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(createExchangeRate())

        // when & then

        shouldThrowExactly<ValidatorsException> { expenseService.create(groupMembers, expense) }
        verify(expenseRepository, times(0)).create(anyVararg(Expense::class))
    }

    should("throw ValidatorsException when both currencies are equal") {
        // given
        val expense = createExpense(baseCurrency = CURRENCY_1, targetCurrency = CURRENCY_1)
        val groupMembers = createGroupMembers(listOf(USER_ID, OTHER_USER_ID))
        whenever(groupManagerClient.getGroupOptions(GROUP_ID)).thenReturn(createGroupOptions(currencies = createCurrencies(CURRENCY_1, CURRENCY_2)))
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(createExchangeRate())

        // when & then

        shouldThrowExactly<ValidatorsException> { expenseService.create(groupMembers, expense) }
        verify(expenseRepository, times(0)).create(anyVararg(Expense::class))
    }

    should("throw ValidatorsException when targetCurrency is present and is not among group currencies") {
        // given
        val expense = createExpense()
        val groupMembers = createGroupMembers(listOf(USER_ID, OTHER_USER_ID))
        whenever(groupManagerClient.getGroupOptions(GROUP_ID)).thenReturn(createGroupOptions(currencies = createCurrencies(CURRENCY_1)))
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(createExchangeRate())

        // when & then

        shouldThrowExactly<ValidatorsException> { expenseService.create(groupMembers, expense) }
        verify(expenseRepository, times(0)).create(anyVararg(Expense::class))
    }

    should("throw ValidatorsException when baseCurrency not available") {
        // given
        val expense = createExpense()
        val groupMembers = createGroupMembers(listOf(USER_ID, OTHER_USER_ID))
        whenever(groupManagerClient.getGroupOptions(GROUP_ID)).thenReturn(createGroupOptions(currencies = createCurrencies(CURRENCY_1)))
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(createExchangeRate())

        // when & then

        shouldThrowExactly<ValidatorsException> { expenseService.create(groupMembers, expense) }
        verify(expenseRepository, times(0)).create(anyVararg(Expense::class))
    }

    should("throw ValidatorsException when baseCurrency not available") {
        // given
        val expense = createExpense()
        val groupMembers = createGroupMembers(listOf(USER_ID, OTHER_USER_ID))
        whenever(groupManagerClient.getGroupOptions(GROUP_ID)).thenReturn(createGroupOptions(currencies = createCurrencies(CURRENCY_1)))
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(createExchangeRate())

        // when & then

        shouldThrowExactly<ValidatorsException> { expenseService.create(groupMembers, expense) }
        verify(expenseRepository, times(0)).create(anyVararg(Expense::class))
    }
},)
