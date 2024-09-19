package pl.edu.agh.gem.internal.service

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.group.createGroupMembers
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.client.AttachmentStoreClient
import pl.edu.agh.gem.internal.client.CurrencyManagerClient
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.model.attachment.GroupAttachment
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseAction.CREATED
import pl.edu.agh.gem.internal.model.expense.ExpenseAction.EDITED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.REJECTED
import pl.edu.agh.gem.internal.persistence.ArchivedExpenseRepository
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_AVAILABLE
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.internal.validation.ValidationMessage.CREATOR_IN_PARTICIPANTS
import pl.edu.agh.gem.internal.validation.ValidationMessage.DUPLICATED_PARTICIPANT
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_COSTS_HIGHER_THAN_TOTAL_COST
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_NOT_GROUP_MEMBER
import pl.edu.agh.gem.internal.validation.ValidationMessage.TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.internal.validation.ValidationMessage.USER_NOT_CREATOR
import pl.edu.agh.gem.internal.validation.ValidationMessage.USER_NOT_PARTICIPANT
import pl.edu.agh.gem.util.DummyData.ANOTHER_USER_ID
import pl.edu.agh.gem.util.DummyData.ATTACHMENT_ID
import pl.edu.agh.gem.util.DummyData.CURRENCY_1
import pl.edu.agh.gem.util.DummyData.CURRENCY_2
import pl.edu.agh.gem.util.DummyData.EXCHANGE_RATE_VALUE
import pl.edu.agh.gem.util.DummyData.EXPENSE_ID
import pl.edu.agh.gem.util.createCurrencies
import pl.edu.agh.gem.util.createExchangeRate
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createExpenseCreation
import pl.edu.agh.gem.util.createExpenseDecision
import pl.edu.agh.gem.util.createExpenseParticipantCost
import pl.edu.agh.gem.util.createExpenseParticipants
import pl.edu.agh.gem.util.createExpenseUpdate
import pl.edu.agh.gem.util.createExpenseUpdateFromExpense
import pl.edu.agh.gem.util.createFilterOptions
import pl.edu.agh.gem.util.createGroup
import pl.edu.agh.gem.validator.ValidatorsException
import java.math.BigDecimal
import java.time.Instant

class ExpenseServiceTest : ShouldSpec({
    val groupManagerClient = mock<GroupManagerClient> { }
    val currencyManagerClient = mock<CurrencyManagerClient> {}
    val attachmentStoreClient = mock<AttachmentStoreClient> {}
    val expenseRepository = mock<ExpenseRepository> {}
    val archivedExpenseRepository = mock<ArchivedExpenseRepository> {}
    val expenseService = ExpenseService(
        groupManagerClient = groupManagerClient,
        currencyManagerClient = currencyManagerClient,
        attachmentStoreClient = attachmentStoreClient,
        expenseRepository = expenseRepository,
        archivedExpenseRepository = archivedExpenseRepository,
    )

    should("create expense when attachmentId is provided") {
        // given
        val expenseCreation = createExpenseCreation()
        val group = createGroup(currencies = createCurrencies(CURRENCY_1, CURRENCY_2))
        val exchangeRate = createExchangeRate()
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(exchangeRate)
        whenever(expenseRepository.save(anyVararg(Expense::class))).thenAnswer { it.arguments[0] }

        // when
        val result = expenseService.create(group, expenseCreation)

        // then
        result.also {
            it.id.shouldNotBeNull()
            it.groupId shouldBe GROUP_ID
            it.creatorId shouldBe USER_ID
            it.title shouldBe expenseCreation.title
            it.totalCost shouldBe expenseCreation.totalCost

            it.targetCurrency shouldBe expenseCreation.targetCurrency
            it.exchangeRate shouldBe exchangeRate

            it.createdAt.shouldNotBeNull()
            it.updatedAt.shouldNotBeNull()
            it.attachmentId shouldBe expenseCreation.attachmentId
            it.status shouldBe PENDING
            it.history shouldHaveSize 1
            it.history.first().also { entry ->
                entry.createdAt.shouldNotBeNull()
                entry.expenseAction shouldBe CREATED
                entry.participantId shouldBe USER_ID
                entry.comment shouldBe expenseCreation.message
            }
        }

        verify(currencyManagerClient, times(1)).getAvailableCurrencies()
        verify(currencyManagerClient, times(1)).getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))
        verify(expenseRepository, times(1)).save(anyVararg(Expense::class))
    }

    should("create expense when attachmentId is not provided") {
        // given
        val expenseCreation = createExpenseCreation(attachmentId = null)
        val group = createGroup(currencies = createCurrencies(CURRENCY_1, CURRENCY_2))
        val exchangeRate = createExchangeRate()
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(exchangeRate)
        whenever(attachmentStoreClient.generateBlankAttachment(GROUP_ID, USER_ID)).thenReturn(GroupAttachment(ATTACHMENT_ID))
        whenever(expenseRepository.save(anyVararg(Expense::class))).thenAnswer { it.arguments[0] }

        // when
        val result = expenseService.create(group, expenseCreation)

        // then
        result.also {
            it.id.shouldNotBeNull()
            it.groupId shouldBe GROUP_ID
            it.creatorId shouldBe USER_ID
            it.title shouldBe expenseCreation.title
            it.totalCost shouldBe expenseCreation.totalCost

            it.targetCurrency shouldBe expenseCreation.targetCurrency
            it.exchangeRate shouldBe exchangeRate

            it.createdAt.shouldNotBeNull()
            it.updatedAt.shouldNotBeNull()
            it.attachmentId shouldBe ATTACHMENT_ID
            it.status shouldBe PENDING
            it.history shouldHaveSize 1
            it.history.first().also { entry ->
                entry.createdAt.shouldNotBeNull()
                entry.expenseAction shouldBe CREATED
                entry.participantId shouldBe USER_ID
                entry.comment shouldBe expenseCreation.message
            }
        }

        verify(currencyManagerClient, times(1)).getAvailableCurrencies()
        verify(currencyManagerClient, times(1)).getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))
        verify(attachmentStoreClient, times(1)).generateBlankAttachment(GROUP_ID, USER_ID)
        verify(expenseRepository, times(1)).save(anyVararg(Expense::class))
    }

    context("throw ValidatorsException cause:") {
        withData(
            nameFn = { it.first },
            Quadruple(
                PARTICIPANT_COSTS_HIGHER_THAN_TOTAL_COST,
                createExpenseCreation(totalCost = BigDecimal.ONE),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(
                CREATOR_IN_PARTICIPANTS,
                createExpenseCreation(expenseParticipantsCost = listOf(createExpenseParticipantCost(participantId = USER_ID))),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(
                DUPLICATED_PARTICIPANT,
                createExpenseCreation(expenseParticipantsCost = listOf(createExpenseParticipantCost(), createExpenseParticipantCost())),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(
                PARTICIPANT_NOT_GROUP_MEMBER,
                createExpenseCreation(
                    expenseParticipantsCost = listOf(
                        createExpenseParticipantCost(),
                        createExpenseParticipantCost("notGroupMember"),
                    ),
                ),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(
                BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES,
                createExpenseCreation(targetCurrency = null),
                arrayOf(),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(
                BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY,
                createExpenseCreation(baseCurrency = CURRENCY_1, targetCurrency = CURRENCY_1),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES, createExpenseCreation(), arrayOf(CURRENCY_1), arrayOf(CURRENCY_1, CURRENCY_2)),
            Quadruple(BASE_CURRENCY_NOT_AVAILABLE, createExpenseCreation(), arrayOf(CURRENCY_1, CURRENCY_2), arrayOf(CURRENCY_2)),

        ) { (expectedMessage, expenseCreation, groupCurrencies, availableCurrencies) ->
            // given
            val group = createGroup(currencies = createCurrencies(*groupCurrencies))
            whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(*availableCurrencies))
            whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L)))
                .thenReturn(createExchangeRate())

            // when & then
            shouldThrowWithMessage<ValidatorsException>("Failed validations: $expectedMessage") {
                expenseService.create(group, expenseCreation)
            }
            verify(expenseRepository, times(0)).save(anyVararg(Expense::class))
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

    should("get group activities") {
        // given
        val expenses = listOf(createExpense())
        val filterOptions = createFilterOptions()
        whenever(expenseRepository.findByGroupId(GROUP_ID, filterOptions)).thenReturn(expenses)

        // when
        val result = expenseService.getGroupActivities(GROUP_ID, filterOptions)

        // then
        result shouldBe expenses
        verify(expenseRepository, times(1)).findByGroupId(GROUP_ID, filterOptions)
    }

    should("return empty list when group has no expenses") {
        // given
        val filterOptions = createFilterOptions()

        whenever(expenseRepository.findByGroupId(GROUP_ID, filterOptions)).thenReturn(listOf())

        // when
        val result = expenseService.getGroupActivities(GROUP_ID, filterOptions)

        // then
        result shouldBe listOf()
        verify(expenseRepository, times(1)).findByGroupId(GROUP_ID, filterOptions)
    }

    should("decide") {
        // given
        val expense = createExpense()
        whenever(expenseRepository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)).thenReturn(expense)

        val expenseDecision = createExpenseDecision(userId = OTHER_USER_ID)

        // when
        expenseService.decide(expenseDecision)

        // then
        verify(expenseRepository, times(1)).findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)
        verify(expenseRepository, times(1)).save(anyVararg(Expense::class))
    }

    should("throw MissingExpenseException when expense is not present") {
        // given
        whenever(expenseRepository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)).thenReturn(null)
        val expenseDecision = createExpenseDecision()

        // when & then
        shouldThrowExactly<MissingExpenseException> { expenseService.decide(expenseDecision) }
        verify(expenseRepository, times(1)).findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)
        verify(expenseRepository, times(0)).save(anyVararg(Expense::class))
    }

    should("throw ValidatorsException when user is not participant") {
        // given
        val expense = createExpense()
        whenever(expenseRepository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)).thenReturn(expense)

        val expenseDecision = createExpenseDecision(userId = "notParticipant")

        // when & then
        shouldThrowWithMessage<ValidatorsException>("Failed validations: $USER_NOT_PARTICIPANT") { expenseService.decide(expenseDecision) }
        verify(expenseRepository, times(1)).findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)
        verify(expenseRepository, times(0)).save(anyVararg(Expense::class))
    }

    should("delete expense") {
        // given
        val expense = createExpense(id = EXPENSE_ID, groupId = GROUP_ID, creatorId = USER_ID)
        whenever(expenseRepository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)).thenReturn(expense)

        // when
        expenseService.deleteExpense(EXPENSE_ID, GROUP_ID, USER_ID)

        // then
        verify(expenseRepository, times(1)).findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)
        verify(expenseRepository, times(1)).delete(expense)
        verify(archivedExpenseRepository, times(1)).add(expense)
    }

    should("throw MissingExpenseException when expense does not exist") {
        // given
        val expense = createExpense(id = EXPENSE_ID, groupId = GROUP_ID, creatorId = USER_ID)
        whenever(expenseRepository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)).thenReturn(null)

        // when & then
        shouldThrowExactly<MissingExpenseException> { expenseService.deleteExpense(EXPENSE_ID, GROUP_ID, USER_ID) }
        verify(expenseRepository, times(1)).findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)
        verify(expenseRepository, times(0)).delete(expense)
        verify(archivedExpenseRepository, times(0)).add(expense)
    }

    should("throw ValidatorsException when user is not expense Creator") {
        // given
        val expense = createExpense(id = EXPENSE_ID, groupId = GROUP_ID, creatorId = OTHER_USER_ID)
        whenever(expenseRepository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)).thenReturn(expense)

        // when & then
        shouldThrowWithMessage<ValidatorsException>("Failed validations: $USER_NOT_CREATOR") {
            expenseService.deleteExpense(EXPENSE_ID, GROUP_ID, USER_ID)
        }
        verify(expenseRepository, times(1)).findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)
        verify(expenseRepository, times(0)).delete(expense)
        verify(archivedExpenseRepository, times(0)).add(expense)
    }

    should("get user expenses") {
        // given
        val expenses = listOf(
            createExpense(
                creatorId = USER_ID,
                totalCost = BigDecimal("60"),
                baseCurrency = CURRENCY_1,
                targetCurrency = null,
                exchangeRate = null,
                expenseParticipants = createExpenseParticipants(
                    listOf(USER_ID, "userId2", "userId3"),
                    listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
                ),
                status = ACCEPTED,
            ),

            createExpense(
                creatorId = OTHER_USER_ID,
                totalCost = BigDecimal("60"),
                baseCurrency = CURRENCY_1,
                targetCurrency = CURRENCY_2,
                exchangeRate = EXCHANGE_RATE_VALUE,
                expenseParticipants = createExpenseParticipants(
                    listOf(USER_ID, OTHER_USER_ID, "userId3"),
                    listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
                ),
                status = ACCEPTED,
            ),

        )

        whenever(expenseRepository.findByGroupId(GROUP_ID)).thenReturn(expenses)

        // when
        val result = expenseService.getUserExpenses(GROUP_ID, USER_ID)

        // then
        result.also {
            it shouldHaveSize 2
            it.first().also { userExpenses ->
                userExpenses.value shouldBe BigDecimal("50")
                userExpenses.currency shouldBe CURRENCY_1
                userExpenses.exchangeRate.shouldBeNull()
            }

            it.last().also { userExpenses ->
                userExpenses.value shouldBe BigDecimal("-10")
                userExpenses.currency shouldBe CURRENCY_2
                userExpenses.exchangeRate shouldBe createExchangeRate()
            }
        }
        verify(expenseRepository, times(1)).findByGroupId(GROUP_ID)
    }

    should("get internal expenses") {
        // given
        val acceptedExpense = createExpense(status = ACCEPTED)
        val expenses = listOf(
            acceptedExpense,
            createExpense(status = PENDING),
            createExpense(status = REJECTED),
        )
        whenever(expenseRepository.findByGroupId(GROUP_ID)).thenReturn(expenses)

        // when
        val result = expenseService.getInternalGroupExpenses(GROUP_ID)

        // then
        result.also {
            it shouldHaveSize 1
            it.first() shouldBe acceptedExpense
        }
        verify(expenseRepository, times(1)).findByGroupId(GROUP_ID)
    }

    should("return empty list when group has no accepted expenses") {
        // given
        whenever(expenseRepository.findByGroupId(GROUP_ID)).thenReturn(
            listOf(
                createExpense(status = PENDING),
                createExpense(status = REJECTED),
            ),
        )

        // when
        val result = expenseService.getInternalGroupExpenses(GROUP_ID)

        // then
        result shouldBe listOf()
        verify(expenseRepository, times(1)).findByGroupId(GROUP_ID)
    }

    should("throw MissingExpenseException when updating expense and expense does not exist") {
        // given
        val expenseUpdate = createExpenseUpdate(id = EXPENSE_ID, groupId = GROUP_ID, userId = USER_ID)
        val group = createGroup(currencies = createCurrencies(CURRENCY_1, CURRENCY_2))

        whenever(expenseRepository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)).thenReturn(null)

        // when & then
        shouldThrowExactly<MissingExpenseException> { expenseService.updateExpense(group, expenseUpdate) }
        verify(expenseRepository, times(1)).findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)
        verify(expenseRepository, times(0)).save(anyVararg(Expense::class))
    }

    context("throw ValidatorsException when updating exception cause:") {
        withData(
            nameFn = { it.first },
            Quadruple(
                PARTICIPANT_COSTS_HIGHER_THAN_TOTAL_COST,
                createExpenseUpdate(totalCost = BigDecimal.ONE),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(
                CREATOR_IN_PARTICIPANTS,
                createExpenseUpdate(
                    expenseParticipants = listOf(
                        createExpenseParticipantCost(USER_ID),
                    ),
                ),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(
                DUPLICATED_PARTICIPANT,
                createExpenseUpdate(expenseParticipants = listOf(createExpenseParticipantCost(), createExpenseParticipantCost())),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(
                PARTICIPANT_NOT_GROUP_MEMBER,
                createExpenseUpdate(expenseParticipants = listOf(createExpenseParticipantCost(), createExpenseParticipantCost("notGroupMember"))),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES, createExpenseUpdate(targetCurrency = null), arrayOf(), arrayOf(CURRENCY_1, CURRENCY_2)),
            Quadruple(
                BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY,
                createExpenseUpdate(baseCurrency = CURRENCY_1, targetCurrency = CURRENCY_1),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
            Quadruple(TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES, createExpenseUpdate(), arrayOf(CURRENCY_1), arrayOf(CURRENCY_1, CURRENCY_2)),
            Quadruple(BASE_CURRENCY_NOT_AVAILABLE, createExpenseUpdate(), arrayOf(CURRENCY_1, CURRENCY_2), arrayOf(CURRENCY_2)),
            Quadruple(
                USER_NOT_CREATOR,
                createExpenseUpdate(userId = ANOTHER_USER_ID),
                arrayOf(CURRENCY_1, CURRENCY_2),
                arrayOf(CURRENCY_1, CURRENCY_2),
            ),
        ) { (expectedMessage, expenseUpdate, groupCurrencies, availableCurrencies) ->
            // given
            val expense = createExpense(id = EXPENSE_ID, groupId = GROUP_ID, creatorId = USER_ID)
            val group = createGroup(createGroupMembers(USER_ID, OTHER_USER_ID, ANOTHER_USER_ID), currencies = createCurrencies(*groupCurrencies))
            whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(*availableCurrencies))
            whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(createExchangeRate())
            whenever(expenseRepository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)).thenReturn(expense)

            // when & then
            shouldThrowWithMessage<ValidatorsException>("Failed validations: $expectedMessage") {
                expenseService.updateExpense(group, expenseUpdate)
            }
            verify(expenseRepository, times(0)).save(anyVararg(Expense::class))
        }
    }

    should("update expense") {
        // given
        val expense = createExpense(id = EXPENSE_ID, groupId = GROUP_ID, creatorId = USER_ID)
        val expenseUpdate = createExpenseUpdate(
            totalCost = BigDecimal(6),
            expenseParticipants = listOf(
                createExpenseParticipantCost(OTHER_USER_ID, BigDecimal.ONE),
                createExpenseParticipantCost(ANOTHER_USER_ID, BigDecimal(4)),
            ),
        )

        val exchangeRate = createExchangeRate(EXCHANGE_RATE_VALUE)
        val group = createGroup(createGroupMembers(USER_ID, OTHER_USER_ID, ANOTHER_USER_ID), currencies = createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(expenseRepository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)).thenReturn(expense)
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(exchangeRate)
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(expenseRepository.save(anyVararg(Expense::class))).doAnswer { it.arguments[0] as? Expense }
        // when & then
        val result = expenseService.updateExpense(group, expenseUpdate)
        verify(expenseRepository, times(1)).findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)
        verify(currencyManagerClient, times(1)).getAvailableCurrencies()
        verify(expenseRepository, times(1)).save(anyVararg(Expense::class))

        result.also {
            it.id shouldBe EXPENSE_ID
            it.groupId shouldBe GROUP_ID
            it.creatorId shouldBe USER_ID
            it.title shouldBe expenseUpdate.title
            it.totalCost shouldBe expenseUpdate.totalCost
            it.baseCurrency shouldBe expenseUpdate.baseCurrency
            it.targetCurrency shouldBe expense.targetCurrency
            it.exchangeRate shouldBe exchangeRate
            it.createdAt shouldBe expense.createdAt
            it.updatedAt.shouldNotBeNull()
            it.attachmentId shouldBe expense.attachmentId
            it.expenseParticipants shouldContainExactly expenseUpdate.expenseParticipantsCost
                .map { p -> p.toExpenseParticipant() }
            it.status shouldBe PENDING
            it.history shouldContainAll expense.history
            it.history.last().also { entry ->
                entry.participantId shouldBe USER_ID
                entry.createdAt.shouldNotBeNull()
                entry.expenseAction shouldBe EDITED
                entry.comment shouldBe expenseUpdate.message
            }
        }
    }

    should("update expense when data did not changed") {
        // given
        val expense = createExpense(id = EXPENSE_ID, groupId = GROUP_ID, creatorId = USER_ID)
        val expenseUpdate = createExpenseUpdateFromExpense(
            expense,
        )

        val exchangeRate = createExchangeRate(EXCHANGE_RATE_VALUE)
        val group = createGroup(createGroupMembers(USER_ID, OTHER_USER_ID, ANOTHER_USER_ID), currencies = createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(expenseRepository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)).thenReturn(expense)
        whenever(currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))).thenReturn(exchangeRate)
        whenever(currencyManagerClient.getAvailableCurrencies()).thenReturn(createCurrencies(CURRENCY_1, CURRENCY_2))
        whenever(expenseRepository.save(anyVararg(Expense::class))).doAnswer { it.arguments[0] as? Expense }
        // when & then
        val result = expenseService.updateExpense(group, expenseUpdate)
        verify(expenseRepository, times(1)).findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID)
        verify(currencyManagerClient, times(1)).getAvailableCurrencies()
        verify(expenseRepository, times(1)).save(anyVararg(Expense::class))

        result.also {
            it.id shouldBe EXPENSE_ID
            it.groupId shouldBe GROUP_ID
            it.creatorId shouldBe USER_ID
            it.title shouldBe expenseUpdate.title
            it.totalCost shouldBe expenseUpdate.totalCost
            it.baseCurrency shouldBe expenseUpdate.baseCurrency
            it.targetCurrency shouldBe expense.targetCurrency
            it.exchangeRate shouldBe exchangeRate
            it.createdAt shouldBe expense.createdAt
            it.updatedAt.shouldNotBeNull()
            it.attachmentId shouldBe expense.attachmentId
            it.expenseParticipants shouldContainExactly expenseUpdate.expenseParticipantsCost
                .map { p -> p.toExpenseParticipant() }
            it.status shouldBe PENDING
            it.history shouldContainAll expense.history
            it.history.last().also { entry ->
                entry.participantId shouldBe USER_ID
                entry.createdAt.shouldNotBeNull()
                entry.expenseAction shouldBe EDITED
                entry.comment shouldBe expenseUpdate.message
            }
        }
    }
},)

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
