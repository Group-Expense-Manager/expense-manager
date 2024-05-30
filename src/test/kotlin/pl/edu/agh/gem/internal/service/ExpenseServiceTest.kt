package pl.edu.agh.gem.internal.service

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
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
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_AVAILABLE
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.internal.validation.ValidationMessage.COST_NOT_SUM_UP
import pl.edu.agh.gem.internal.validation.ValidationMessage.DUPLICATED_PARTICIPANT
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_MIN_SIZE
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_NOT_GROUP_MEMBER
import pl.edu.agh.gem.internal.validation.ValidationMessage.TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.internal.validation.ValidationMessage.USER_NOT_PARTICIPANT
import pl.edu.agh.gem.util.DummyData.CURRENCY_1
import pl.edu.agh.gem.util.DummyData.CURRENCY_2
import pl.edu.agh.gem.util.DummyData.EXPENSE_ID
import pl.edu.agh.gem.util.createCurrencies
import pl.edu.agh.gem.util.createExchangeRate
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createExpenseParticipant
import pl.edu.agh.gem.util.createGroup
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
        val groupMembers = createGroupMembers(USER_ID, OTHER_USER_ID)
        whenever(groupManagerClient.getMembers(GROUP_ID)).thenReturn(groupMembers)

        // when
        val result = expenseService.getMembers(GROUP_ID)

        // then
        verify(groupManagerClient, atLeastOnce()).getMembers(GROUP_ID)
        result shouldBe groupMembers
    }

    should("create expense") {
        // given
        val expense = createExpense()
        val group = createGroup(currencies = createCurrencies(CURRENCY_1, CURRENCY_2))
        val exchangeRate = createExchangeRate()
        val expected = expense.copy(exchangeRate = exchangeRate)
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(exchangeRate)
        whenever(expenseRepository.create(anyVararg(Expense::class))).thenReturn(expected)

        // when
        val result = expenseService.create(group, expense)

        // then
        result shouldBe expected

        verify(currencyManagerClient, times(1)).getAvailableCurrencies()
        verify(currencyManagerClient, times(1)).getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))
        verify(expenseRepository, times(1)).create(anyVararg(Expense::class))
    }

    context("throw ValidatorsException cause:") {
        withData(
            nameFn = { it.first },
            Quadruple(COST_NOT_SUM_UP, createExpense(cost = BigDecimal.TEN), arrayOf(CURRENCY_1, CURRENCY_2), arrayOf(CURRENCY_1, CURRENCY_2)),
            Quadruple(
                USER_NOT_PARTICIPANT,
                createExpense(creatorId = "nonGroupMember"),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(
                DUPLICATED_PARTICIPANT,
                createExpense(expenseParticipants = listOf(createExpenseParticipant(), createExpenseParticipant())),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(
                PARTICIPANT_MIN_SIZE,
                createExpense(cost = BigDecimal.TWO, expenseParticipants = listOf(createExpenseParticipant())),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(
                PARTICIPANT_NOT_GROUP_MEMBER,
                createExpense(expenseParticipants = listOf(createExpenseParticipant(), createExpenseParticipant("notGroupMember"))),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES, createExpense(targetCurrency = null), arrayOf(), arrayOf(CURRENCY_1, CURRENCY_2)),
            Quadruple(
                BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY,
                createExpense(baseCurrency = CURRENCY_1, targetCurrency = CURRENCY_1),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES, createExpense(), arrayOf(CURRENCY_1), arrayOf(CURRENCY_1, CURRENCY_2)),
            Quadruple(BASE_CURRENCY_NOT_AVAILABLE, createExpense(), arrayOf(CURRENCY_1, CURRENCY_2), arrayOf(CURRENCY_2)),

        ) { (expectedMessage, expense, groupCurrencies, availableCurrencies) ->
            // given
            val group = createGroup(currencies = createCurrencies(*groupCurrencies))
            whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(*availableCurrencies))
            whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(createExchangeRate())

            // when & then
            shouldThrowWithMessage<ValidatorsException>("Failed validations: $expectedMessage") { expenseService.create(group, expense) }
            verify(expenseRepository, times(0)).create(anyVararg(Expense::class))
        }
    }

    should("get expense") {
        // given
        val expense = createExpense()
        whenever(expenseRepository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)).thenReturn(expense)

        // when
        val result = expenseService.getExpense(EXPENSE_ID, GROUP_ID)

        // then
        result shouldBe expense
        verify(expenseRepository, times(1)).findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)
    }

    should("throw MissingExpenseException when there is no expense for given id & groupId") {
        // given
        whenever(expenseRepository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)).thenReturn(null)

        // when & then
        shouldThrowExactly<MissingExpenseException> { expenseService.getExpense(EXPENSE_ID, GROUP_ID) }
        verify(expenseRepository, times(1)).findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)
    }
},)

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
