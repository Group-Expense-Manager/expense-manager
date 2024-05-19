package pl.edu.agh.gem.util

import pl.edu.agh.gem.external.dto.currency.CurrenciesResponse
import pl.edu.agh.gem.external.dto.currency.ExchangeRateResponse
import pl.edu.agh.gem.external.dto.expense.ExpenseCreationRequest
import pl.edu.agh.gem.external.dto.expense.ExpenseParticipantRequestData
import pl.edu.agh.gem.external.dto.group.GroupOptionsResponse
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.model.currency.Currencies
import pl.edu.agh.gem.internal.model.currency.Currency
import pl.edu.agh.gem.internal.model.currency.ExchangeRate
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseAction
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipant
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.StatusHistoryEntry
import pl.edu.agh.gem.internal.model.group.GroupOptions
import pl.edu.agh.gem.util.DummyData.ATTACHMENT_ID
import pl.edu.agh.gem.util.DummyData.CURRENCY_1
import pl.edu.agh.gem.util.DummyData.CURRENCY_2
import pl.edu.agh.gem.util.DummyData.EXCHANGE_RATE_VALUE
import pl.edu.agh.gem.util.DummyData.EXPENSE_ID
import java.math.BigDecimal
import java.time.Instant
import java.time.Instant.now

fun createExpenseCreationRequest(
    title: String = "My Expense",
    cost: BigDecimal = BigDecimal(10),
    baseCurrency: String = CURRENCY_1,
    targetCurrency: String? = CURRENCY_2,
    expenseDate: Instant = Instant.ofEpochMilli(0L),
    expenseParticipants: List<ExpenseParticipantRequestData> = listOf(
        createExpenseParticipantDto(USER_ID, BigDecimal.ONE),
        createExpenseParticipantDto(OTHER_USER_ID, BigDecimal.valueOf(9L)),
    ),

    attachmentId: String? = "1234-1234-ffff",
) = ExpenseCreationRequest(
    title = title,
    cost = cost,
    baseCurrency = baseCurrency,
    targetCurrency = targetCurrency,
    expenseDate = expenseDate,
    expenseParticipants = expenseParticipants,
    attachmentId = attachmentId,
)

fun createExpenseParticipantDto(
    participantId: String = USER_ID,
    cost: BigDecimal = BigDecimal(1),
) = ExpenseParticipantRequestData(
    participantId = participantId,
    participantCost = cost,
)

fun createCurrencies(
    vararg currencies: String = arrayOf(CURRENCY_1),
) = Currencies(currencies.map { Currency(it) })

fun createCurrenciesResponse(
    vararg currencies: String = arrayOf(CURRENCY_1),
) = CurrenciesResponse(currencies.map { Currency(it) })

fun createExchangeRate(
    value: BigDecimal = EXCHANGE_RATE_VALUE,
) = ExchangeRate(value)

fun createExchangeRateResponse(
    value: BigDecimal = EXCHANGE_RATE_VALUE,
) = ExchangeRateResponse(value)

fun createExpense(
    id: String = EXPENSE_ID,
    groupId: String = GROUP_ID,
    creatorId: String = USER_ID,
    title: String = "Some title",
    cost: BigDecimal = BigDecimal.valueOf(4L),
    baseCurrency: String = CURRENCY_1,
    targetCurrency: String? = CURRENCY_2,
    exchangeRate: BigDecimal? = EXCHANGE_RATE_VALUE,
    createdAt: Instant = now(),
    updatedAt: Instant = now(),
    expenseDate: Instant = Instant.ofEpochMilli(0L),
    attachmentId: String? = ATTACHMENT_ID,
    expenseParticipants: List<ExpenseParticipant> = listOf(createExpenseParticipant(USER_ID), createExpenseParticipant(OTHER_USER_ID)),
    status: ExpenseStatus = PENDING,
    statusHistory: List<StatusHistoryEntry> = arrayListOf(StatusHistoryEntry(USER_ID, ExpenseAction.CREATED)),
) = Expense(
    id = id,
    groupId = groupId,
    creatorId = creatorId,
    title = title,
    cost = cost,
    baseCurrency = baseCurrency,
    targetCurrency = targetCurrency,
    exchangeRate = exchangeRate?.let { ExchangeRate(it) },
    createdAt = createdAt,
    updatedAt = updatedAt,
    expenseDate = expenseDate,
    attachmentId = attachmentId,
    expenseParticipants = expenseParticipants,
    status = status,
    statusHistory = statusHistory,
)

fun createExpenseParticipant(
    participantId: String = USER_ID,
    participantCost: BigDecimal = BigDecimal.TWO,
    participantStatus: ExpenseStatus = PENDING,
) = ExpenseParticipant(
    participantId = participantId,
    participantCost = participantCost,
    participantStatus = participantStatus,
)
fun createGroupOptions(
    acceptRequired: Boolean = false,
    currencies: Currencies = createCurrencies(CURRENCY_1),
) = GroupOptions(
    acceptRequired = acceptRequired,
    currencies = currencies,
)

fun createGroupOptionsResponse(
    acceptRequired: Boolean = false,
    currencies: Currencies = createCurrencies(CURRENCY_1),
) = GroupOptionsResponse(
    acceptRequired = acceptRequired,
    currencies = currencies,
)

object DummyData {
    const val EXPENSE_ID = "expenseId"
    const val CURRENCY_1 = "PLN"
    const val CURRENCY_2 = "EUR"
    const val CURRENCY_3 = "USD"
    const val ATTACHMENT_ID = "attachmentId"
    val EXCHANGE_RATE_VALUE = BigDecimal.TWO
}
