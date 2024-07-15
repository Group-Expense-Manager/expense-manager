package pl.edu.agh.gem.integration.controller

import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.assertion.shouldBody
import pl.edu.agh.gem.assertion.shouldHaveErrors
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.assertion.shouldHaveValidationError
import pl.edu.agh.gem.assertion.shouldHaveValidatorError
import pl.edu.agh.gem.dto.GroupMemberResponse
import pl.edu.agh.gem.dto.GroupMembersResponse
import pl.edu.agh.gem.exception.UserWithoutGroupAccessException
import pl.edu.agh.gem.external.dto.expense.ExpenseResponse
import pl.edu.agh.gem.external.dto.expense.GroupExpensesResponse
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.group.createGroupMembers
import pl.edu.agh.gem.helper.user.DummyUser.EMAIL
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.helper.user.createGemUser
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.integration.ability.stubCurrencyManagerAvailableCurrencies
import pl.edu.agh.gem.integration.ability.stubCurrencyManagerExchangeRate
import pl.edu.agh.gem.integration.ability.stubGroupManagerGroup
import pl.edu.agh.gem.integration.ability.stubGroupManagerMembers
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.internal.service.ExpenseDeletionAccessException
import pl.edu.agh.gem.internal.service.MissingExpenseException
import pl.edu.agh.gem.internal.service.UserNotParticipantException
import pl.edu.agh.gem.internal.validation.ValidationMessage.ATTACHMENT_ID_NULL_OR_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_AVAILABLE
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_PATTERN
import pl.edu.agh.gem.internal.validation.ValidationMessage.COST_NOT_SUM_UP
import pl.edu.agh.gem.internal.validation.ValidationMessage.CREATOR_DECISION
import pl.edu.agh.gem.internal.validation.ValidationMessage.DUPLICATED_PARTICIPANT
import pl.edu.agh.gem.internal.validation.ValidationMessage.EXPENSE_ID_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.EXPENSE_PARTICIPANTS_NOT_EMPTY
import pl.edu.agh.gem.internal.validation.ValidationMessage.GROUP_ID_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.MESSAGE_NULL_OR_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_ID_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_MIN_SIZE
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_NOT_GROUP_MEMBER
import pl.edu.agh.gem.internal.validation.ValidationMessage.POSITIVE_COST
import pl.edu.agh.gem.internal.validation.ValidationMessage.POSITIVE_PARTICIPANT_COST
import pl.edu.agh.gem.internal.validation.ValidationMessage.TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.internal.validation.ValidationMessage.TARGET_CURRENCY_PATTERN
import pl.edu.agh.gem.internal.validation.ValidationMessage.TITLE_MAX_LENGTH
import pl.edu.agh.gem.internal.validation.ValidationMessage.TITLE_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.USER_NOT_PARTICIPANT
import pl.edu.agh.gem.util.DummyData.CURRENCY_1
import pl.edu.agh.gem.util.DummyData.CURRENCY_2
import pl.edu.agh.gem.util.DummyData.EXCHANGE_RATE_VALUE
import pl.edu.agh.gem.util.DummyData.EXPENSE_ID
import pl.edu.agh.gem.util.createCurrenciesDTO
import pl.edu.agh.gem.util.createCurrenciesResponse
import pl.edu.agh.gem.util.createExchangeRateResponse
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createExpenseCreationRequest
import pl.edu.agh.gem.util.createExpenseDecisionRequest
import pl.edu.agh.gem.util.createExpenseParticipant
import pl.edu.agh.gem.util.createExpenseParticipantDto
import pl.edu.agh.gem.util.createGroupResponse
import pl.edu.agh.gem.util.createMembersDTO
import java.math.BigDecimal
import java.time.Instant

class ExternalExpenseControllerIT(
    private val service: ServiceTestClient,
    private val repository: ExpenseRepository,
) : BaseIntegrationSpec(
    {
        context("return validation exception cause:") {
            withData(
                nameFn = { it.first },
                Pair(TITLE_NOT_BLANK, createExpenseCreationRequest(title = "")),
                Pair(
                    TITLE_MAX_LENGTH,
                    createExpenseCreationRequest(title = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                ),
                Pair(POSITIVE_COST, createExpenseCreationRequest(cost = BigDecimal.ZERO)),
                Pair(BASE_CURRENCY_NOT_BLANK, createExpenseCreationRequest(baseCurrency = "")),
                Pair(BASE_CURRENCY_PATTERN, createExpenseCreationRequest(baseCurrency = "pln")),
                Pair(TARGET_CURRENCY_PATTERN, createExpenseCreationRequest(targetCurrency = "pln")),
                Pair(EXPENSE_PARTICIPANTS_NOT_EMPTY, createExpenseCreationRequest(expenseParticipants = emptyList())),
                Pair(ATTACHMENT_ID_NULL_OR_NOT_BLANK, createExpenseCreationRequest(attachmentId = "")),
                Pair(
                    PARTICIPANT_ID_NOT_BLANK,
                    createExpenseCreationRequest(expenseParticipants = listOf(createExpenseParticipantDto(participantId = ""))),
                ),
                Pair(
                    POSITIVE_PARTICIPANT_COST,
                    createExpenseCreationRequest(expenseParticipants = listOf(createExpenseParticipantDto(cost = BigDecimal.ZERO))),
                ),

            ) { (expectedMessage, expenseCreationRequest) ->
                // when
                val response = service.createExpense(expenseCreationRequest, createGemUser(), GROUP_ID)

                // then
                response shouldHaveHttpStatus BAD_REQUEST
                response shouldHaveValidationError expectedMessage
            }
        }

        should("create expense") {
            // given
            val createExpenseRequest = createExpenseCreationRequest()
            stubGroupManagerGroup(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
            stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
            stubCurrencyManagerExchangeRate(
                createExchangeRateResponse(value = EXCHANGE_RATE_VALUE),
                CURRENCY_1,
                CURRENCY_2,
                Instant.ofEpochSecond(0L),
            )

            // when
            val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

            // then
            response shouldHaveHttpStatus CREATED
        }

        should("not create expense when user dont have access") {
            // given
            val user = createGemUser()
            val groupId = GROUP_ID
            val createExpenseRequest = createExpenseCreationRequest()
            stubGroupManagerGroup(createGroupResponse(members = createMembersDTO(OTHER_USER_ID)), GROUP_ID)

            // when
            val response = service.createExpense(createExpenseRequest, user, groupId)

            // then
            response shouldHaveHttpStatus FORBIDDEN
        }

        should("return validator exception cause COST_NOT_SUM_UP") {
            // given
            val createExpenseRequest = createExpenseCreationRequest(cost = BigDecimal.TWO)
            stubGroupManagerGroup(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
            stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
            stubCurrencyManagerExchangeRate(
                createExchangeRateResponse(value = EXCHANGE_RATE_VALUE),
                CURRENCY_1,
                CURRENCY_2,
                Instant.ofEpochSecond(0L),
            )

            // when
            val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveValidatorError COST_NOT_SUM_UP
        }

        should("return validator exception cause USER_NOT_PARTICIPANT") {
            // given
            val createExpenseRequest = createExpenseCreationRequest(
                expenseParticipants = listOf(createExpenseParticipantDto(participantId = OTHER_USER_ID)),
            )
            stubGroupManagerGroup(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
            stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
            stubCurrencyManagerExchangeRate(
                createExchangeRateResponse(value = EXCHANGE_RATE_VALUE),
                CURRENCY_1,
                CURRENCY_2,
                Instant.ofEpochSecond(0L),
            )

            // when
            val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveValidatorError USER_NOT_PARTICIPANT
        }

        should("return validator exception cause DUPLICATED_PARTICIPANT") {
            // given
            val createExpenseRequest = createExpenseCreationRequest(
                expenseParticipants = listOf(createExpenseParticipantDto(), createExpenseParticipantDto()),
            )
            stubGroupManagerGroup(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
            stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
            stubCurrencyManagerExchangeRate(
                createExchangeRateResponse(value = EXCHANGE_RATE_VALUE),
                CURRENCY_1,
                CURRENCY_2,
                Instant.ofEpochSecond(0L),
            )

            // when
            val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveValidatorError DUPLICATED_PARTICIPANT
        }

        should("return validator exception cause PARTICIPANT_MIN_SIZE") {
            // given
            val createExpenseRequest = createExpenseCreationRequest(expenseParticipants = listOf(createExpenseParticipantDto()))
            stubGroupManagerGroup(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
            stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
            stubCurrencyManagerExchangeRate(
                createExchangeRateResponse(value = EXCHANGE_RATE_VALUE),
                CURRENCY_1,
                CURRENCY_2,
                Instant.ofEpochSecond(0L),
            )

            // when
            val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveValidatorError PARTICIPANT_MIN_SIZE
        }

        should("return validator exception cause PARTICIPANT_NOT_GROUP_MEMBER") {
            // given
            val createExpenseRequest = createExpenseCreationRequest()
            stubGroupManagerGroup(
                createGroupResponse(members = createMembersDTO(USER_ID), groupCurrencies = createCurrenciesDTO(CURRENCY_2)),
                GROUP_ID,
            )
            stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
            stubCurrencyManagerExchangeRate(
                createExchangeRateResponse(value = EXCHANGE_RATE_VALUE),
                CURRENCY_1,
                CURRENCY_2,
                Instant.ofEpochSecond(0L),
            )

            // when
            val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveValidatorError PARTICIPANT_NOT_GROUP_MEMBER
        }

        should("return validator exception cause BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES") {
            // given
            val createExpenseRequest = createExpenseCreationRequest(targetCurrency = null)
            stubGroupManagerGroup(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
            stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
            stubCurrencyManagerExchangeRate(
                createExchangeRateResponse(value = EXCHANGE_RATE_VALUE),
                CURRENCY_1,
                CURRENCY_2,
                Instant.ofEpochSecond(0L),
            )

            // when
            val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveValidatorError BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES
        }

        should("return validator exception cause BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY") {
            // given
            val createExpenseRequest = createExpenseCreationRequest(baseCurrency = CURRENCY_1, targetCurrency = CURRENCY_1)
            stubGroupManagerGroup(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
            stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
            stubCurrencyManagerExchangeRate(
                createExchangeRateResponse(value = EXCHANGE_RATE_VALUE),
                CURRENCY_1,
                CURRENCY_2,
                Instant.ofEpochSecond(0L),
            )

            // when
            val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveValidatorError BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY
        }

        should("return validator exception cause TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES") {
            // given
            val createExpenseRequest = createExpenseCreationRequest(baseCurrency = CURRENCY_1, targetCurrency = CURRENCY_2)
            stubGroupManagerGroup(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_1)), GROUP_ID)
            stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
            stubCurrencyManagerExchangeRate(
                createExchangeRateResponse(value = EXCHANGE_RATE_VALUE),
                CURRENCY_1,
                CURRENCY_2,
                Instant.ofEpochSecond(0L),
            )

            // when
            val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveValidatorError TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES
        }

        should("return validator exception cause BASE_CURRENCY_NOT_AVAILABLE") {
            // given
            val createExpenseRequest = createExpenseCreationRequest(baseCurrency = CURRENCY_1, targetCurrency = CURRENCY_2)
            stubGroupManagerGroup(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
            stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_2))
            stubCurrencyManagerExchangeRate(
                createExchangeRateResponse(value = EXCHANGE_RATE_VALUE),
                CURRENCY_1,
                CURRENCY_2,
                Instant.ofEpochSecond(0L),
            )

            // when
            val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveValidatorError BASE_CURRENCY_NOT_AVAILABLE
        }

        should("get expense") {
            // given
            val groupMembers = GroupMembersResponse(listOf(GroupMemberResponse(USER_ID), GroupMemberResponse(OTHER_USER_ID)))
            stubGroupManagerMembers(groupMembers, GROUP_ID)
            val expense = createExpense(expenseParticipants = listOf(createExpenseParticipant()))
            repository.create(expense)

            // when
            val response = service.getExpense(createGemUser(USER_ID), expense.id, GROUP_ID)

            // then
            response shouldHaveHttpStatus OK
            response.shouldBody<ExpenseResponse> {
                creatorId shouldBe expense.creatorId
                cost shouldBe expense.cost
                baseCurrency shouldBe expense.baseCurrency
                targetCurrency shouldBe expense.targetCurrency
                exchangeRate shouldBe expense.exchangeRate?.value
                createdAt.shouldNotBeNull()
                updatedAt.shouldNotBeNull()
                expenseDate.shouldNotBeNull()
                attachmentId shouldBe expense.attachmentId
                expenseParticipants shouldHaveSize 1
                expenseParticipants.first().also { participant ->
                    participant.participantId shouldBe expense.expenseParticipants.first().participantId
                    participant.participantCost shouldBe expense.expenseParticipants.first().participantCost
                    participant.participantStatus shouldBe expense.expenseParticipants.first().participantStatus.name
                }
                status shouldBe expense.status.name
                statusHistory shouldHaveSize 1
                statusHistory.first().also { entry ->
                    entry.createdAt.shouldNotBeNull()
                    entry.expenseAction shouldBe expense.statusHistory.first().expenseAction.name
                    entry.participantId shouldBe expense.statusHistory.first().participantId
                    entry.comment shouldBe expense.statusHistory.first().comment
                }
            }
        }

        should("return forbidden if user is not a group member") {
            // given
            val groupMembers = GroupMembersResponse(listOf(GroupMemberResponse(OTHER_USER_ID)))
            stubGroupManagerMembers(groupMembers, GROUP_ID)
            // when
            val response = service.getExpense(createGemUser(USER_ID), EXPENSE_ID, GROUP_ID)

            // then
            response shouldHaveHttpStatus FORBIDDEN
        }

        should("return not found when expense doesn't exist") {
            // given
            val groupMembers = GroupMembersResponse(listOf(GroupMemberResponse(USER_ID), GroupMemberResponse(OTHER_USER_ID)))
            stubGroupManagerMembers(groupMembers, GROUP_ID)
            // when
            val response = service.getExpense(createGemUser(USER_ID), EXPENSE_ID, GROUP_ID)

            // then
            response shouldHaveHttpStatus NOT_FOUND
        }

        should("get expenses") {
            // given
            val groupMembers = GroupMembersResponse(listOf(GroupMemberResponse(USER_ID)))
            stubGroupManagerMembers(groupMembers, GROUP_ID)
            val expense = createExpense(expenseParticipants = listOf(createExpenseParticipant()))
            repository.create(expense)

            // when
            val response = service.getGroupExpenses(createGemUser(USER_ID), GROUP_ID)

            // then
            response shouldHaveHttpStatus OK
            response.shouldBody<GroupExpensesResponse> {
                expenses shouldHaveSize 1
                expenses.first().also {
                    it.expenseId shouldBe expense.id
                    it.creatorId shouldBe expense.creatorId
                    it.title shouldBe expense.title
                    it.cost shouldBe expense.cost
                    it.baseCurrency shouldBe expense.baseCurrency
                    it.status shouldBe expense.status.name
                    it.participantIds.shouldHaveSize(1)
                    it.participantIds.first() shouldBe expense.expenseParticipants.first().participantId
                    it.expenseDate shouldBe expense.expenseDate
                }
            }
        }

        should("return forbidden if user is not a group member") {
            // given
            val groupMembers = GroupMembersResponse(listOf(GroupMemberResponse(OTHER_USER_ID)))
            stubGroupManagerMembers(groupMembers, GROUP_ID)
            // when
            val response = service.getGroupExpenses(createGemUser(USER_ID), GROUP_ID)

            // then
            response shouldHaveHttpStatus FORBIDDEN
            response shouldHaveErrors {
                errors shouldHaveSize 1
                errors.first().code shouldBe UserWithoutGroupAccessException::class.simpleName
            }
        }

        context("return validation exception cause:") {
            withData(
                nameFn = { it.first },
                Pair(EXPENSE_ID_NOT_BLANK, createExpenseDecisionRequest(expenseId = "")),
                Pair(GROUP_ID_NOT_BLANK, createExpenseDecisionRequest(groupId = "")),
                Pair(MESSAGE_NULL_OR_NOT_BLANK, createExpenseDecisionRequest(message = "")),

            ) { (expectedMessage, expenseDecisionRequest) ->
                // when
                val response = service.decide(expenseDecisionRequest, createGemUser())

                // then
                response shouldHaveHttpStatus BAD_REQUEST
                response shouldHaveValidationError expectedMessage
            }
        }

        should("decide") {
            // given
            val decisionRequest = createExpenseDecisionRequest()
            val groupMembersResponse = createGroupMembers(USER_ID, OTHER_USER_ID)
            stubGroupManagerMembers(groupMembersResponse, GROUP_ID)
            val expense = createExpense()
            repository.create(expense)

            // when
            val response = service.decide(decisionRequest, createGemUser(id = OTHER_USER_ID))

            // then
            response shouldHaveHttpStatus OK
        }

        should("return forbidden if user is not a group member") {
            // given
            val decisionRequest = createExpenseDecisionRequest()
            val groupMembersResponse = createGroupMembers(USER_ID)
            stubGroupManagerMembers(groupMembersResponse, GROUP_ID)

            // when
            val response = service.decide(decisionRequest, createGemUser(id = OTHER_USER_ID))

            // then
            response shouldHaveHttpStatus FORBIDDEN
            response shouldHaveErrors {
                errors shouldHaveSize 1
                errors.first().code shouldBe UserWithoutGroupAccessException::class.simpleName
            }
        }

        should("return not found when expense is not present") {
            // given
            val decisionRequest = createExpenseDecisionRequest()
            val groupMembersResponse = createGroupMembers(USER_ID, OTHER_USER_ID)
            stubGroupManagerMembers(groupMembersResponse, GROUP_ID)

            // when
            val response = service.decide(decisionRequest, createGemUser(id = OTHER_USER_ID))

            // then
            response shouldHaveHttpStatus NOT_FOUND
            response shouldHaveErrors {
                errors shouldHaveSize 1
                errors.first().code shouldBe MissingExpenseException::class.simpleName
            }
        }

        should("return bad request when user is expense creator") {
            // given
            val decisionRequest = createExpenseDecisionRequest()
            val groupMembersResponse = createGroupMembers(USER_ID, OTHER_USER_ID)
            stubGroupManagerMembers(groupMembersResponse, GROUP_ID)
            val expense = createExpense()
            repository.create(expense)

            // when
            val response = service.decide(decisionRequest, createGemUser(id = USER_ID))

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveValidatorError CREATOR_DECISION
        }

        should("return forbidden when group member is not expense participant") {
            // given
            val decisionRequest = createExpenseDecisionRequest()
            val anotherUserId = "AnotherUserId"
            val groupMembersResponse = createGroupMembers(USER_ID, OTHER_USER_ID, anotherUserId)
            stubGroupManagerMembers(groupMembersResponse, GROUP_ID)
            val expense = createExpense()
            repository.create(expense)

            // when
            val response = service.decide(decisionRequest, createGemUser(id = anotherUserId))

            // then
            response shouldHaveHttpStatus FORBIDDEN
            response shouldHaveErrors {
                errors shouldHaveSize 1
                errors.first().code shouldBe UserNotParticipantException::class.simpleName
            }
        }

        should("delete expense") {
            // given
            val expense = createExpense(id = EXPENSE_ID, groupId = GROUP_ID, creatorId = USER_ID)
            repository.create(expense)
            stubGroupManagerMembers(createGroupMembers(USER_ID, OTHER_USER_ID), GROUP_ID)

            // when
            val response = service.delete(createGemUser(USER_ID, EMAIL), GROUP_ID, EXPENSE_ID)

            // then
            response shouldHaveHttpStatus OK
            repository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID).also {
                it.shouldBeNull()
            }
        }
        should("return forbidden if user is not a group member") {
            // given
            stubGroupManagerMembers(createGroupMembers(OTHER_USER_ID), GROUP_ID)

            // when
            val response = service.delete(createGemUser(USER_ID, EMAIL), GROUP_ID, EXPENSE_ID)

            // then
            response shouldHaveHttpStatus FORBIDDEN
            response shouldHaveErrors {
                errors shouldHaveSize 1
                errors.first().code shouldBe UserWithoutGroupAccessException::class.simpleName
            }
        }

        should("return not found when expense is not present") {
            // given
            stubGroupManagerMembers(createGroupMembers(USER_ID, OTHER_USER_ID), GROUP_ID)

            // when
            val response = service.delete(createGemUser(USER_ID, EMAIL), GROUP_ID, EXPENSE_ID)

            // then
            response shouldHaveHttpStatus NOT_FOUND
            response shouldHaveErrors {
                errors shouldHaveSize 1
                errors.first().code shouldBe MissingExpenseException::class.simpleName
            }
        }

        should("return forbidden when user is not expense creator") {
            // given
            val expense = createExpense(id = EXPENSE_ID, groupId = GROUP_ID, creatorId = OTHER_USER_ID)
            repository.create(expense)
            stubGroupManagerMembers(createGroupMembers(USER_ID, OTHER_USER_ID), GROUP_ID)

            // when
            val response = service.delete(createGemUser(USER_ID, EMAIL), GROUP_ID, EXPENSE_ID)

            // then
            response shouldHaveHttpStatus FORBIDDEN
            response shouldHaveErrors {
                errors shouldHaveSize 1
                errors.first().code shouldBe ExpenseDeletionAccessException::class.simpleName
            }
        }
    },
)
