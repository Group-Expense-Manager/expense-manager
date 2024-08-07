package pl.edu.agh.gem.integration.controller

import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
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
import pl.edu.agh.gem.exception.UserWithoutGroupAccessException
import pl.edu.agh.gem.external.dto.expense.ExpenseResponse
import pl.edu.agh.gem.external.dto.expense.ExpenseUpdateResponse
import pl.edu.agh.gem.external.dto.expense.toExpenseUpdateParticipant
import pl.edu.agh.gem.external.dto.group.CurrencyDTO
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.group.DummyGroup.OTHER_GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.EMAIL
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.helper.user.createGemUser
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.integration.ability.stubCurrencyManagerAvailableCurrencies
import pl.edu.agh.gem.integration.ability.stubCurrencyManagerExchangeRate
import pl.edu.agh.gem.integration.ability.stubGroupManagerGroupData
import pl.edu.agh.gem.integration.ability.stubGroupManagerUserGroups
import pl.edu.agh.gem.internal.model.expense.ExpenseAction.EDITED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.toExpenseUpdateParticipant
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.internal.service.ExpenseDeletionAccessException
import pl.edu.agh.gem.internal.service.ExpenseUpdateAccessException
import pl.edu.agh.gem.internal.service.MissingExpenseException
import pl.edu.agh.gem.internal.service.NoExpenseUpdateException
import pl.edu.agh.gem.internal.service.Quadruple
import pl.edu.agh.gem.internal.service.UserNotParticipantException
import pl.edu.agh.gem.internal.validation.ValidationMessage.ATTACHMENT_ID_NOT_BLANK
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
import pl.edu.agh.gem.util.DummyData.ANOTHER_USER_ID
import pl.edu.agh.gem.util.DummyData.CURRENCY_1
import pl.edu.agh.gem.util.DummyData.CURRENCY_2
import pl.edu.agh.gem.util.DummyData.EXCHANGE_RATE_VALUE
import pl.edu.agh.gem.util.DummyData.EXPENSE_ID
import pl.edu.agh.gem.util.createCurrenciesDTO
import pl.edu.agh.gem.util.createCurrenciesResponse
import pl.edu.agh.gem.util.createExchangeRate
import pl.edu.agh.gem.util.createExchangeRateResponse
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createExpenseCreationRequest
import pl.edu.agh.gem.util.createExpenseDecisionRequest
import pl.edu.agh.gem.util.createExpenseParticipant
import pl.edu.agh.gem.util.createExpenseParticipantDto
import pl.edu.agh.gem.util.createExpenseUpdateRequest
import pl.edu.agh.gem.util.createExpenseUpdateRequestFromExpense
import pl.edu.agh.gem.util.createGroupResponse
import pl.edu.agh.gem.util.createMembersDTO
import pl.edu.agh.gem.util.createUserGroupsResponse
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
                Pair(ATTACHMENT_ID_NOT_BLANK, createExpenseCreationRequest(attachmentId = "")),
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
            stubGroupManagerGroupData(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
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
            stubGroupManagerGroupData(createGroupResponse(members = createMembersDTO(OTHER_USER_ID)), GROUP_ID)

            // when
            val response = service.createExpense(createExpenseRequest, user, groupId)

            // then
            response shouldHaveHttpStatus FORBIDDEN
        }

        should("return validator exception cause COST_NOT_SUM_UP") {
            // given
            val createExpenseRequest = createExpenseCreationRequest(cost = BigDecimal.TWO)
            stubGroupManagerGroupData(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
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
            stubGroupManagerGroupData(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
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
            stubGroupManagerGroupData(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
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
            stubGroupManagerGroupData(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
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
            stubGroupManagerGroupData(
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
            stubGroupManagerGroupData(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
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
            stubGroupManagerGroupData(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
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
            stubGroupManagerGroupData(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_1)), GROUP_ID)
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
            stubGroupManagerGroupData(createGroupResponse(groupCurrencies = createCurrenciesDTO(CURRENCY_2)), GROUP_ID)
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
            stubGroupManagerUserGroups(createUserGroupsResponse(GROUP_ID, OTHER_GROUP_ID), USER_ID)
            val expense = createExpense(expenseParticipants = listOf(createExpenseParticipant()))
            repository.save(expense)

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
            stubGroupManagerUserGroups(createUserGroupsResponse(OTHER_GROUP_ID), USER_ID)

            // when
            val response = service.getExpense(createGemUser(USER_ID), EXPENSE_ID, GROUP_ID)

            // then
            response shouldHaveHttpStatus FORBIDDEN
        }

        should("return not found when expense doesn't exist") {
            // given
            stubGroupManagerUserGroups(createUserGroupsResponse(GROUP_ID, OTHER_GROUP_ID), USER_ID)

            // when
            val response = service.getExpense(createGemUser(USER_ID), EXPENSE_ID, GROUP_ID)

            // then
            response shouldHaveHttpStatus NOT_FOUND
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
            stubGroupManagerUserGroups(createUserGroupsResponse(GROUP_ID, OTHER_GROUP_ID), OTHER_USER_ID)

            val expense = createExpense()
            repository.save(expense)

            // when
            val response = service.decide(decisionRequest, createGemUser(id = OTHER_USER_ID))

            // then
            response shouldHaveHttpStatus OK
        }

        should("return forbidden if user is not a group member") {
            // given
            val decisionRequest = createExpenseDecisionRequest()
            stubGroupManagerUserGroups(createUserGroupsResponse(OTHER_GROUP_ID), USER_ID)

            // when
            val response = service.decide(decisionRequest, createGemUser(id = USER_ID))

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
            stubGroupManagerUserGroups(createUserGroupsResponse(GROUP_ID, OTHER_GROUP_ID), USER_ID)

            // when
            val response = service.decide(decisionRequest, createGemUser(id = USER_ID))

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
            stubGroupManagerUserGroups(createUserGroupsResponse(GROUP_ID, OTHER_GROUP_ID), USER_ID)

            val expense = createExpense()
            repository.save(expense)

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
            stubGroupManagerUserGroups(createUserGroupsResponse(GROUP_ID), anotherUserId)

            val expense = createExpense()
            repository.save(expense)

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
            repository.save(expense)
            stubGroupManagerUserGroups(createUserGroupsResponse(GROUP_ID, OTHER_GROUP_ID), USER_ID)

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
            stubGroupManagerUserGroups(createUserGroupsResponse(OTHER_GROUP_ID), USER_ID)

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
            stubGroupManagerUserGroups(createUserGroupsResponse(GROUP_ID, OTHER_GROUP_ID), USER_ID)

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
            repository.save(expense)
            stubGroupManagerUserGroups(createUserGroupsResponse(GROUP_ID, OTHER_GROUP_ID), USER_ID)

            // when
            val response = service.delete(createGemUser(USER_ID, EMAIL), GROUP_ID, EXPENSE_ID)

            // then
            response shouldHaveHttpStatus FORBIDDEN
            response shouldHaveErrors {
                errors shouldHaveSize 1
                errors.first().code shouldBe ExpenseDeletionAccessException::class.simpleName
            }
        }

        context("return validation exception when updating expense cause:") {
            withData(
                nameFn = { it.first },
                Pair(TITLE_NOT_BLANK, createExpenseUpdateRequest(title = "")),
                Pair(
                    TITLE_MAX_LENGTH,
                    createExpenseUpdateRequest(title = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                ),
                Pair(POSITIVE_COST, createExpenseUpdateRequest(cost = BigDecimal.ZERO)),
                Pair(BASE_CURRENCY_NOT_BLANK, createExpenseUpdateRequest(baseCurrency = "")),
                Pair(BASE_CURRENCY_PATTERN, createExpenseUpdateRequest(baseCurrency = "pln")),
                Pair(TARGET_CURRENCY_PATTERN, createExpenseUpdateRequest(targetCurrency = "pln")),
                Pair(EXPENSE_PARTICIPANTS_NOT_EMPTY, createExpenseUpdateRequest(expenseParticipants = emptyList())),
                Pair(
                    PARTICIPANT_ID_NOT_BLANK,
                    createExpenseUpdateRequest(expenseParticipants = listOf(createExpenseParticipantDto(participantId = ""))),
                ),
                Pair(
                    POSITIVE_PARTICIPANT_COST,
                    createExpenseUpdateRequest(expenseParticipants = listOf(createExpenseParticipantDto(cost = BigDecimal.ZERO))),
                ),
                Pair(MESSAGE_NULL_OR_NOT_BLANK, createExpenseUpdateRequest(message = "")),
            ) { (expectedMessage, expenseUpdateRequest) ->
                // when
                val response = service.updateExpense(expenseUpdateRequest, createGemUser(), GROUP_ID, EXPENSE_ID)

                // then
                response shouldHaveHttpStatus BAD_REQUEST
                response shouldHaveValidationError expectedMessage
            }
        }

        should("not update expense when user doesn't have access") {
            // given
            val user = createGemUser()
            val expenseUpdateRequest = createExpenseUpdateRequest()
            stubGroupManagerGroupData(createGroupResponse(members = createMembersDTO(OTHER_USER_ID)), GROUP_ID)

            // when
            val response = service.updateExpense(expenseUpdateRequest, user, GROUP_ID, EXPENSE_ID)

            // then
            response shouldHaveHttpStatus FORBIDDEN
        }

        should("return not found when updating expense and expense is not present") {
            // given
            val expenseUpdateRequest = createExpenseUpdateRequest()
            stubGroupManagerGroupData(createGroupResponse(members = createMembersDTO(USER_ID, OTHER_USER_ID)), GROUP_ID)

            // when
            val response = service.updateExpense(expenseUpdateRequest, createGemUser(USER_ID), GROUP_ID, EXPENSE_ID)

            // then
            response shouldHaveHttpStatus NOT_FOUND
            response shouldHaveErrors {
                errors shouldHaveSize 1
                errors.first().code shouldBe MissingExpenseException::class.simpleName
            }
        }

        should("return forbidden when updating expense and user is not expense creator") {
            // given
            val expense = createExpense(id = EXPENSE_ID, groupId = GROUP_ID, creatorId = OTHER_USER_ID)
            val expenseUpdateRequest = createExpenseUpdateRequest()
            repository.save(expense)
            stubGroupManagerGroupData(createGroupResponse(members = createMembersDTO(USER_ID, OTHER_USER_ID)), GROUP_ID)

            // when
            val response = service.updateExpense(expenseUpdateRequest, createGemUser(USER_ID), GROUP_ID, EXPENSE_ID)

            // then
            response shouldHaveHttpStatus FORBIDDEN
            response shouldHaveErrors {
                errors shouldHaveSize 1
                errors.first().code shouldBe ExpenseUpdateAccessException::class.simpleName
            }
        }

        context("return validator exception when updating exception cause:") {
            withData(
                nameFn = { it.first },
                Quadruple(
                    COST_NOT_SUM_UP,
                    createExpenseUpdateRequest(cost = BigDecimal.TWO),
                    listOf(CURRENCY_1, CURRENCY_2),
                    arrayOf(CURRENCY_1, CURRENCY_2),
                ),
                Quadruple(
                    USER_NOT_PARTICIPANT,
                    createExpenseUpdateRequest(
                        expenseParticipants = listOf(
                            createExpenseParticipantDto(OTHER_USER_ID),
                            createExpenseParticipantDto(ANOTHER_USER_ID),

                        ),
                    ),
                    listOf(CURRENCY_1, CURRENCY_2),
                    arrayOf(CURRENCY_1, CURRENCY_2),
                ),
                Quadruple(
                    DUPLICATED_PARTICIPANT,
                    createExpenseUpdateRequest(expenseParticipants = listOf(createExpenseParticipantDto(), createExpenseParticipantDto())),
                    listOf(CURRENCY_1, CURRENCY_2),
                    arrayOf(CURRENCY_1, CURRENCY_2),
                ),
                Quadruple(
                    PARTICIPANT_MIN_SIZE,
                    createExpenseUpdateRequest(cost = BigDecimal.TWO, expenseParticipants = listOf(createExpenseParticipantDto())),
                    listOf(CURRENCY_1, CURRENCY_2),
                    arrayOf(CURRENCY_1, CURRENCY_2),
                ),
                Quadruple(
                    PARTICIPANT_NOT_GROUP_MEMBER,
                    createExpenseUpdateRequest(
                        expenseParticipants = listOf(createExpenseParticipantDto(), createExpenseParticipantDto("notGroupMember")),
                    ),
                    listOf(CURRENCY_1, CURRENCY_2),
                    arrayOf(CURRENCY_1, CURRENCY_2),
                ),
                Quadruple(
                    BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES,
                    createExpenseUpdateRequest(targetCurrency = null),
                    listOf(),
                    arrayOf(CURRENCY_1, CURRENCY_2),
                ),
                Quadruple(
                    BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY,
                    createExpenseUpdateRequest(baseCurrency = CURRENCY_1, targetCurrency = CURRENCY_1),
                    listOf(CURRENCY_1, CURRENCY_2),
                    arrayOf(CURRENCY_1, CURRENCY_2),
                ),
                Quadruple(TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES, createExpenseUpdateRequest(), listOf(CURRENCY_1), arrayOf(CURRENCY_1, CURRENCY_2)),
                Quadruple(BASE_CURRENCY_NOT_AVAILABLE, createExpenseUpdateRequest(), listOf(CURRENCY_1, CURRENCY_2), arrayOf(CURRENCY_2)),

            ) { (expectedMessage, updateExpenseRequest, groupCurrencies, availableCurrencies) ->

                // given
                val expense = createExpense(id = EXPENSE_ID, groupId = GROUP_ID, creatorId = USER_ID)
                repository.save(expense)
                stubGroupManagerGroupData(
                    createGroupResponse(createMembersDTO(USER_ID, OTHER_USER_ID), groupCurrencies = groupCurrencies.map { CurrencyDTO(it) }),
                    GROUP_ID,
                )
                stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(*availableCurrencies))

                // when
                val response = service.updateExpense(updateExpenseRequest, createGemUser(USER_ID), GROUP_ID, EXPENSE_ID)

                // then
                response shouldHaveHttpStatus BAD_REQUEST
                response shouldHaveValidatorError expectedMessage
            }
        }
        should("return bad request when updating expense and update doesn't change anything") {
            // given
            val expense = createExpense(id = EXPENSE_ID, groupId = GROUP_ID, creatorId = USER_ID)
            val expenseUpdateRequest = createExpenseUpdateRequestFromExpense(expense)
            repository.save(expense)
            stubGroupManagerGroupData(createGroupResponse(members = createMembersDTO(USER_ID, OTHER_USER_ID)), GROUP_ID)

            // when
            val response = service.updateExpense(expenseUpdateRequest, createGemUser(USER_ID), GROUP_ID, EXPENSE_ID)

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveErrors {
                errors shouldHaveSize 1
                errors.first().code shouldBe NoExpenseUpdateException::class.simpleName
            }
        }

        should("update expense") {
            // given
            val expense = createExpense(id = EXPENSE_ID, groupId = GROUP_ID, creatorId = USER_ID)
            val expenseUpdateRequest = createExpenseUpdateRequest(
                cost = BigDecimal(6),
                expenseParticipants = listOf(
                    createExpenseParticipantDto(USER_ID, BigDecimal.ONE),
                    createExpenseParticipantDto(OTHER_USER_ID, BigDecimal.ONE),
                    createExpenseParticipantDto(ANOTHER_USER_ID, BigDecimal(4)),
                ),
            )
            repository.save(expense)
            stubGroupManagerGroupData(createGroupResponse(members = createMembersDTO(USER_ID, OTHER_USER_ID, ANOTHER_USER_ID)), GROUP_ID)
            stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
            stubCurrencyManagerExchangeRate(
                createExchangeRateResponse(value = EXCHANGE_RATE_VALUE),
                CURRENCY_1,
                CURRENCY_2,
                Instant.ofEpochSecond(0L),
            )
            // when
            val response = service.updateExpense(expenseUpdateRequest, createGemUser(USER_ID), GROUP_ID, EXPENSE_ID)

            // then
            response shouldHaveHttpStatus OK
            response.shouldBody<ExpenseUpdateResponse> {
                expenseId shouldBe EXPENSE_ID
            }
            repository.findByExpenseIdAndGroupId(EXPENSE_ID, GROUP_ID).also {
                it.shouldNotBeNull()
                it.id shouldBe EXPENSE_ID
                it.groupId shouldBe GROUP_ID
                it.creatorId shouldBe USER_ID
                it.title shouldBe expenseUpdateRequest.title
                it.cost shouldBe expenseUpdateRequest.cost
                it.baseCurrency shouldBe expenseUpdateRequest.baseCurrency
                it.targetCurrency shouldBe expenseUpdateRequest.targetCurrency
                it.exchangeRate shouldBe createExchangeRate(EXCHANGE_RATE_VALUE)
                it.createdAt.shouldNotBeNull()
                it.updatedAt.shouldNotBeNull()
                it.attachmentId shouldBe expense.attachmentId
                it.expenseParticipants shouldContainExactly expenseUpdateRequest.expenseParticipants
                    .map { p -> p.toExpenseUpdateParticipant().toExpenseParticipant(USER_ID) }
                it.status shouldBe PENDING
                it.statusHistory.last().also { statusHistory ->
                    statusHistory.participantId shouldBe USER_ID
                    statusHistory.createdAt.shouldNotBeNull()
                    statusHistory.expenseAction shouldBe EDITED
                    statusHistory.comment shouldBe expenseUpdateRequest.message
                }
            }
        }
    },

)
