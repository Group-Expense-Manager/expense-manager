package pl.edu.agh.gem.integration.controller

import io.kotest.datatest.withData
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.FORBIDDEN
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.assertion.shouldHaveValidationError
import pl.edu.agh.gem.assertion.shouldHaveValidatorError
import pl.edu.agh.gem.external.dto.currency.ExchangeRateResponse
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.helper.user.createGemUser
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.integration.ability.stubCurrencyManagerAvailableCurrencies
import pl.edu.agh.gem.integration.ability.stubCurrencyManagerExchangeRate
import pl.edu.agh.gem.integration.ability.stubGroupManagerGroup
import pl.edu.agh.gem.internal.validation.ValidationMessage.ATTACHMENT_ID_NOT_NULL_AND_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_AVAILABLE
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_PATTERN
import pl.edu.agh.gem.internal.validation.ValidationMessage.COST_NOT_SUM_UP
import pl.edu.agh.gem.internal.validation.ValidationMessage.DUPLICATED_PARTICIPANT
import pl.edu.agh.gem.internal.validation.ValidationMessage.EXPENSE_PARTICIPANTS_NOT_EMPTY
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
import pl.edu.agh.gem.util.createCurrenciesResponse
import pl.edu.agh.gem.util.createExpenseCreationRequest
import pl.edu.agh.gem.util.createExpenseParticipantDto
import pl.edu.agh.gem.util.createGroupResponse
import java.math.BigDecimal
import java.time.Instant

class ExpenseControllerIT(
    private val service: ServiceTestClient,
) : BaseIntegrationSpec({
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
            Pair(ATTACHMENT_ID_NOT_NULL_AND_BLANK, createExpenseCreationRequest(attachmentId = "")),
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
        stubGroupManagerGroup(createGroupResponse(currencies = listOf(CURRENCY_2)), GROUP_ID)
        stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
        stubCurrencyManagerExchangeRate(ExchangeRateResponse(EXCHANGE_RATE_VALUE), CURRENCY_1, CURRENCY_2, Instant.ofEpochSecond(0L))

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
        stubGroupManagerGroup(createGroupResponse(members = listOf(OTHER_USER_ID)), GROUP_ID)


        // when
        val response = service.createExpense(createExpenseRequest, user, groupId)

        // then
        response shouldHaveHttpStatus FORBIDDEN
    }

    should("return validator exception cause COST_NOT_SUM_UP") {
        // given
        val createExpenseRequest = createExpenseCreationRequest(cost = BigDecimal.TWO)
        stubGroupManagerGroup(createGroupResponse(currencies = listOf(CURRENCY_2)), GROUP_ID)
        stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
        stubCurrencyManagerExchangeRate(ExchangeRateResponse(EXCHANGE_RATE_VALUE), CURRENCY_1, CURRENCY_2, Instant.ofEpochSecond(0L))

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
        stubGroupManagerGroup(createGroupResponse(currencies = listOf(CURRENCY_2)), GROUP_ID)
        stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
        stubCurrencyManagerExchangeRate(ExchangeRateResponse(EXCHANGE_RATE_VALUE), CURRENCY_1, CURRENCY_2, Instant.ofEpochSecond(0L))

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
        stubGroupManagerGroup(createGroupResponse(currencies = listOf(CURRENCY_2)), GROUP_ID)
        stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
        stubCurrencyManagerExchangeRate(ExchangeRateResponse(EXCHANGE_RATE_VALUE), CURRENCY_1, CURRENCY_2, Instant.ofEpochSecond(0L))

        // when
        val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidatorError DUPLICATED_PARTICIPANT
    }

    should("return validator exception cause PARTICIPANT_MIN_SIZE") {
        // given
        val createExpenseRequest = createExpenseCreationRequest(expenseParticipants = listOf(createExpenseParticipantDto()))
        stubGroupManagerGroup(createGroupResponse(currencies = listOf(CURRENCY_2)), GROUP_ID)
        stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
        stubCurrencyManagerExchangeRate(ExchangeRateResponse(EXCHANGE_RATE_VALUE), CURRENCY_1, CURRENCY_2, Instant.ofEpochSecond(0L))

        // when
        val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidatorError PARTICIPANT_MIN_SIZE
    }

    should("return validator exception cause PARTICIPANT_NOT_GROUP_MEMBER") {
        // given
        val createExpenseRequest = createExpenseCreationRequest()
        stubGroupManagerGroup(createGroupResponse(members = listOf(USER_ID), currencies = listOf(CURRENCY_2)), GROUP_ID)
        stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
        stubCurrencyManagerExchangeRate(ExchangeRateResponse(EXCHANGE_RATE_VALUE), CURRENCY_1, CURRENCY_2, Instant.ofEpochSecond(0L))

        // when
        val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidatorError PARTICIPANT_NOT_GROUP_MEMBER
    }

    should("return validator exception cause BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES") {
        // given
        val createExpenseRequest = createExpenseCreationRequest(targetCurrency = null)
        stubGroupManagerGroup(createGroupResponse(currencies = listOf(CURRENCY_2)), GROUP_ID)
        stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
        stubCurrencyManagerExchangeRate(ExchangeRateResponse(EXCHANGE_RATE_VALUE), CURRENCY_1, CURRENCY_2, Instant.ofEpochSecond(0L))

        // when
        val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidatorError BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES
    }

    should("return validator exception cause BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY") {
        // given
        val createExpenseRequest = createExpenseCreationRequest(baseCurrency = CURRENCY_1, targetCurrency = CURRENCY_1)
        stubGroupManagerGroup(createGroupResponse(currencies = listOf(CURRENCY_2)), GROUP_ID)
        stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
        stubCurrencyManagerExchangeRate(ExchangeRateResponse(EXCHANGE_RATE_VALUE), CURRENCY_1, CURRENCY_2, Instant.ofEpochSecond(0L))

        // when
        val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidatorError BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY
    }

    should("return validator exception cause TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES") {
        // given
        val createExpenseRequest = createExpenseCreationRequest(baseCurrency = CURRENCY_1, targetCurrency = CURRENCY_2)
        stubGroupManagerGroup(createGroupResponse(currencies = listOf(CURRENCY_1)), GROUP_ID)
        stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_1, CURRENCY_2))
        stubCurrencyManagerExchangeRate(ExchangeRateResponse(EXCHANGE_RATE_VALUE), CURRENCY_1, CURRENCY_2, Instant.ofEpochSecond(0L))

        // when
        val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidatorError TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES
    }

    should("return validator exception cause BASE_CURRENCY_NOT_AVAILABLE") {
        // given
        val createExpenseRequest = createExpenseCreationRequest(baseCurrency = CURRENCY_1, targetCurrency = CURRENCY_2)
        stubGroupManagerGroup(createGroupResponse(currencies = listOf(CURRENCY_2)), GROUP_ID)
        stubCurrencyManagerAvailableCurrencies(createCurrenciesResponse(CURRENCY_2))
        stubCurrencyManagerExchangeRate(ExchangeRateResponse(EXCHANGE_RATE_VALUE), CURRENCY_1, CURRENCY_2, Instant.ofEpochSecond(0L))

        // when
        val response = service.createExpense(createExpenseRequest, createGemUser(USER_ID), GROUP_ID)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidatorError BASE_CURRENCY_NOT_AVAILABLE
    }
},)
