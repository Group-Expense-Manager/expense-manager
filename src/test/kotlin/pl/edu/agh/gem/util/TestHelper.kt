package pl.edu.agh.gem.util

import pl.edu.agh.gem.external.dto.currency.CurrenciesResponse
import pl.edu.agh.gem.external.dto.currency.ExchangeRateResponse
import pl.edu.agh.gem.external.dto.expense.AcceptedGroupExpenseParticipantDto
import pl.edu.agh.gem.external.dto.expense.AmountDto
import pl.edu.agh.gem.external.dto.expense.ExpenseCreationRequest
import pl.edu.agh.gem.external.dto.expense.ExpenseDecisionRequest
import pl.edu.agh.gem.external.dto.expense.ExpenseParticipantRequestData
import pl.edu.agh.gem.external.dto.expense.ExpenseUpdateRequest
import pl.edu.agh.gem.external.dto.expense.toAmountDto
import pl.edu.agh.gem.external.dto.group.CurrencyDTO
import pl.edu.agh.gem.external.dto.group.GroupDto
import pl.edu.agh.gem.external.dto.group.GroupResponse
import pl.edu.agh.gem.external.dto.group.MemberDTO
import pl.edu.agh.gem.external.dto.group.UserGroupsResponse
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.group.DummyGroup.OTHER_GROUP_ID
import pl.edu.agh.gem.helper.group.createGroupMembers
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.model.currency.Currency
import pl.edu.agh.gem.internal.model.expense.Amount
import pl.edu.agh.gem.internal.model.expense.Decision
import pl.edu.agh.gem.internal.model.expense.Decision.ACCEPT
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseAction
import pl.edu.agh.gem.internal.model.expense.ExpenseCreation
import pl.edu.agh.gem.internal.model.expense.ExpenseDecision
import pl.edu.agh.gem.internal.model.expense.ExpenseHistoryEntry
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipant
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipantCost
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.ExpenseUpdate
import pl.edu.agh.gem.internal.model.expense.FxData
import pl.edu.agh.gem.internal.model.expense.UserExpense
import pl.edu.agh.gem.internal.model.expense.filter.FilterOptions
import pl.edu.agh.gem.internal.model.expense.filter.SortOrder
import pl.edu.agh.gem.internal.model.expense.filter.SortOrder.ASCENDING
import pl.edu.agh.gem.internal.model.expense.filter.SortedBy
import pl.edu.agh.gem.internal.model.expense.filter.SortedBy.DATE
import pl.edu.agh.gem.internal.model.expense.toExpenseParticipantCost
import pl.edu.agh.gem.internal.model.group.Currencies
import pl.edu.agh.gem.internal.model.group.GroupData
import pl.edu.agh.gem.model.GroupMembers
import pl.edu.agh.gem.util.DummyData.ATTACHMENT_ID
import pl.edu.agh.gem.util.DummyData.CURRENCY_1
import pl.edu.agh.gem.util.DummyData.CURRENCY_2
import pl.edu.agh.gem.util.DummyData.EXCHANGE_RATE_VALUE
import pl.edu.agh.gem.util.DummyData.EXPENSE_ID
import java.math.BigDecimal
import java.time.Instant
import java.time.Instant.now

fun createAmountDto(
    value: BigDecimal = "10".toBigDecimal(),
    currency: String = CURRENCY_1,
) = AmountDto(
    value = value,
    currency = currency,
)

fun createExpenseCreationRequest(
    title: String = "My Expense",
    amount: AmountDto = createAmountDto(),
    targetCurrency: String? = CURRENCY_2,
    expenseDate: Instant = Instant.ofEpochMilli(0L),
    expenseParticipants: List<ExpenseParticipantRequestData> = listOf(
        createExpenseParticipantDto(OTHER_USER_ID, BigDecimal.valueOf(9L)),
    ),
    message: String? = "Something",
    attachmentId: String? = "1234-1234-ffff",
) = ExpenseCreationRequest(
    title = title,
    amount = amount,
    targetCurrency = targetCurrency,
    expenseDate = expenseDate,
    expenseParticipants = expenseParticipants,
    message = message,
    attachmentId = attachmentId,
)

fun createExpenseParticipantDto(
    participantId: String = OTHER_USER_ID,
    cost: BigDecimal = BigDecimal(1),
) = ExpenseParticipantRequestData(
    participantId = participantId,
    participantCost = cost,
)

fun createCurrencies(
    vararg currencies: String = arrayOf(CURRENCY_1),
) = currencies.map { Currency(it) }

fun createCurrenciesResponse(
    vararg currencies: String = arrayOf(CURRENCY_1),
) = CurrenciesResponse(currencies.map { CurrencyDTO(it) })

fun createExchangeRateResponse(
    currencyFrom: String = CURRENCY_1,
    currencyTo: String = CURRENCY_2,
    value: BigDecimal = EXCHANGE_RATE_VALUE,
    createdAt: Instant = now(),
) = ExchangeRateResponse(
    currencyFrom = currencyFrom,
    currencyTo = currencyTo,
    rate = value,
    createdAt = createdAt,
)

fun createAmount(
    value: BigDecimal = "10".toBigDecimal(),
    currency: String = CURRENCY_1,
) = Amount(
    value = value,
    currency = currency,
)

fun createFxData(
    targetCurrency: String = CURRENCY_2,
    exchangeRate: BigDecimal = EXCHANGE_RATE_VALUE,
) = FxData(
    targetCurrency = targetCurrency,
    exchangeRate = exchangeRate,
)
fun createExpense(
    id: String = EXPENSE_ID,
    groupId: String = GROUP_ID,
    creatorId: String = USER_ID,
    title: String = "Some title",
    amount: Amount = createAmount(value = "4".toBigDecimal(), currency = CURRENCY_1),
    fxData: FxData? = createFxData(),
    createdAt: Instant = now(),
    updatedAt: Instant = now(),
    expenseDate: Instant = Instant.ofEpochMilli(0L),
    attachmentId: String? = ATTACHMENT_ID,
    expenseParticipants: List<ExpenseParticipant> = listOf(createExpenseParticipant(OTHER_USER_ID)),
    status: ExpenseStatus = PENDING,
    history: List<ExpenseHistoryEntry> = arrayListOf(ExpenseHistoryEntry(USER_ID, ExpenseAction.CREATED)),
) = Expense(
    id = id,
    groupId = groupId,
    creatorId = creatorId,
    title = title,
    amount = amount,
    fxData = fxData,
    createdAt = createdAt,
    updatedAt = updatedAt,
    expenseDate = expenseDate,
    attachmentId = attachmentId,
    expenseParticipants = expenseParticipants,
    status = status,
    history = history,
)

fun createExpenseCreation(
    groupId: String = GROUP_ID,
    creatorId: String = USER_ID,
    title: String = "Some title",
    amount: Amount = createAmount(value = "4".toBigDecimal(), currency = CURRENCY_1),
    targetCurrency: String? = CURRENCY_2,
    expenseDate: Instant = Instant.ofEpochMilli(0L),
    attachmentId: String? = ATTACHMENT_ID,
    expenseParticipantsCost: List<ExpenseParticipantCost> = listOf(
        createExpenseParticipantCost(OTHER_USER_ID),
    ),
) = ExpenseCreation(
    groupId = groupId,
    creatorId = creatorId,
    title = title,
    amount = amount,
    targetCurrency = targetCurrency,
    expenseDate = expenseDate,
    attachmentId = attachmentId,
    expenseParticipantsCost = expenseParticipantsCost,
)

fun createExpenseParticipant(
    participantId: String = OTHER_USER_ID,
    participantCost: BigDecimal = BigDecimal.TWO,
    participantStatus: ExpenseStatus = PENDING,
) = ExpenseParticipant(
    participantId = participantId,
    participantCost = participantCost,
    participantStatus = participantStatus,
)

fun createExpenseParticipants(
    ids: List<String> = listOf("userId1", "userId2", "userId3"),
    costs: List<BigDecimal> = listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
    statuses: List<ExpenseStatus> = listOf(ACCEPTED, ACCEPTED, ACCEPTED),
) = ids.mapIndexed { index, id -> createExpenseParticipant(id, costs[index], statuses[index]) }

fun createGroup(
    members: GroupMembers = createGroupMembers(USER_ID, OTHER_USER_ID),
    currencies: Currencies = createCurrencies(CURRENCY_1, CURRENCY_2),
) = GroupData(
    members = members,
    currencies = currencies,
)

fun createGroupResponse(
    members: List<MemberDTO> = listOf(USER_ID, OTHER_USER_ID).map { MemberDTO(it) },
    groupCurrencies: List<CurrencyDTO> = listOf(CURRENCY_1, CURRENCY_2).map { CurrencyDTO(it) },
) = GroupResponse(
    members = members,
    groupCurrencies = groupCurrencies,
)

fun createExpenseDecisionRequest(
    expenseId: String = EXPENSE_ID,
    groupId: String = GROUP_ID,
    decision: Decision = ACCEPT,
    message: String = "Some message",
) = ExpenseDecisionRequest(
    expenseId = expenseId,
    groupId = groupId,
    decision = decision,
    message = message,
)

fun createExpenseDecision(
    userId: String = USER_ID,
    expenseId: String = EXPENSE_ID,
    groupId: String = GROUP_ID,
    decision: Decision = ACCEPT,
    message: String = "Some message",
) = ExpenseDecision(
    userId = userId,
    expenseId = expenseId,
    groupId = groupId,
    decision = decision,
    message = message,
)

fun createCurrenciesDTO(
    vararg currency: String = arrayOf(CURRENCY_1, CURRENCY_2),
) = currency.map { CurrencyDTO(it) }

fun createMembersDTO(
    vararg members: String = arrayOf(USER_ID, OTHER_USER_ID),
) = members.map { MemberDTO(it) }

fun createUserExpense(
    value: BigDecimal = BigDecimal.ONE,
    currency: String = CURRENCY_1,
    exchangeRate: BigDecimal? = null,
) = UserExpense(
    value = value,
    currency = currency,
    exchangeRate = exchangeRate,
)

fun createListOfAcceptedGroupExpenseParticipantDto(
    participantIds: List<String> = listOf("userId1", "userId2", "userId3"),
    participantCosts: List<BigDecimal> = listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
) = participantIds.mapIndexed { index, id -> AcceptedGroupExpenseParticipantDto(id, participantCosts[index]) }

fun createExpenseUpdateRequest(
    title: String = "My Modified Expense",
    amount: AmountDto = createAmountDto(),
    targetCurrency: String? = CURRENCY_2,
    expenseDate: Instant = Instant.ofEpochMilli(0L),
    expenseParticipants: List<ExpenseParticipantRequestData> = listOf(
        createExpenseParticipantDto(OTHER_USER_ID, BigDecimal.valueOf(9L)),
    ),
    message: String? = "Something",
    attachmentId: String? = ATTACHMENT_ID,
) = ExpenseUpdateRequest(
    title = title,
    amount = amount,
    targetCurrency = targetCurrency,
    expenseDate = expenseDate,
    expenseParticipants = expenseParticipants,
    message = message,
    attachmentId = attachmentId,
)

fun createExpenseUpdateRequestFromExpense(
    expense: Expense = createExpense(),
) = ExpenseUpdateRequest(
    title = expense.title,
    amount = expense.amount.toAmountDto(),
    targetCurrency = expense.fxData?.targetCurrency,
    expenseDate = expense.expenseDate,
    expenseParticipants = expense.expenseParticipants.map { createExpenseParticipantDto(it.participantId, it.participantCost) },
    message = "Something",
    attachmentId = expense.attachmentId,
)

fun createExpenseUpdate(
    id: String = EXPENSE_ID,
    groupId: String = GROUP_ID,
    userId: String = USER_ID,
    title: String = "Some modified title",
    amount: Amount = createAmount(value = "4".toBigDecimal(), currency = CURRENCY_1),
    targetCurrency: String? = CURRENCY_2,
    expenseDate: Instant = Instant.ofEpochMilli(0L),
    expenseParticipants: List<ExpenseParticipantCost> = listOf(
        createExpenseParticipantCost(OTHER_USER_ID),
    ),
    message: String? = "Something",
    attachmentId: String? = ATTACHMENT_ID,
) = ExpenseUpdate(
    id = id,
    groupId = groupId,
    userId = userId,
    title = title,
    amount = amount,
    targetCurrency = targetCurrency,
    expenseDate = expenseDate,
    expenseParticipantsCost = expenseParticipants,
    message = message,
    attachmentId = attachmentId,
)

fun createExpenseUpdateFromExpense(
    expense: Expense = createExpense(),
) = ExpenseUpdate(
    id = expense.id,
    groupId = expense.groupId,
    userId = expense.creatorId,
    title = expense.title,
    amount = expense.amount,
    targetCurrency = expense.fxData?.targetCurrency,
    expenseDate = expense.expenseDate,
    expenseParticipantsCost = expense.expenseParticipants.map { it.toExpenseParticipantCost() },
    message = null,
    attachmentId = expense.attachmentId,
)
fun createExpenseParticipantCost(
    participantId: String = OTHER_USER_ID,
    participantCost: BigDecimal = BigDecimal.TWO,
) = ExpenseParticipantCost(
    participantId = participantId,
    participantCost = participantCost,
)

fun createUserGroupsResponse(
    vararg groups: String = arrayOf(GROUP_ID, OTHER_GROUP_ID),
) = UserGroupsResponse(groups = groups.map { GroupDto(it) })

fun createFilterOptions(
    title: String? = null,
    status: ExpenseStatus? = null,
    creatorId: String? = null,
    currency: String? = null,
    sortedBy: SortedBy = DATE,
    sortOrder: SortOrder = ASCENDING,
) = FilterOptions(
    title = title,
    status = status,
    creatorId = creatorId,
    currency = currency,
    sortedBy = sortedBy,
    sortOrder = sortOrder,
)

object DummyData {
    const val EXPENSE_ID = "expenseId"
    const val CURRENCY_1 = "PLN"
    const val CURRENCY_2 = "EUR"
    const val ATTACHMENT_ID = "attachmentId"
    const val ANOTHER_USER_ID = "anotherUserId"
    val EXCHANGE_RATE_VALUE: BigDecimal = BigDecimal.TWO
}

data class Triple<A, B, C>(
    val first: A,
    val second: B,
    val third: C,
)
