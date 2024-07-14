package pl.edu.agh.gem.external.dto.expense

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipant
import java.math.BigDecimal
import java.time.Instant

data class InternalGroupExpensesResponse(
    val groupId: String,
    val expenses: List<InternalGroupExpenseDto>,
)

data class InternalGroupExpenseDto(
    val creatorId: String,
    val title: String,
    val cost: BigDecimal,
    val baseCurrency: String,
    val targetCurrency: String?,
    val exchangeRate: BigDecimal?,
    val participants: List<InternalGroupExpenseParticipantDto>,
    val expenseDate: Instant,
)
fun Expense.toInternalGroupExpenseDto() = InternalGroupExpenseDto(
    creatorId = creatorId,
    title = title,
    cost = cost,
    baseCurrency = baseCurrency,
    targetCurrency = targetCurrency,
    exchangeRate = exchangeRate?.value,
    participants = expenseParticipants.map { it.toInternalGroupExpenseParticipantDto() },
    expenseDate = expenseDate,
)

data class InternalGroupExpenseParticipantDto(
    val participantId: String,
    val participantCost: BigDecimal,
)
private fun ExpenseParticipant.toInternalGroupExpenseParticipantDto() =
    InternalGroupExpenseParticipantDto(
        participantId = participantId,
        participantCost = participantCost,
    )

fun List<Expense>.toInternalGroupExpensesResponse(groupId: String) = InternalGroupExpensesResponse(
    groupId = groupId,
    expenses = map { it.toInternalGroupExpenseDto() },
)
