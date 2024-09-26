package pl.edu.agh.gem.external.dto.expense

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipant
import java.math.BigDecimal
import java.time.Instant

data class AcceptedGroupExpensesResponse(
    val groupId: String,
    val expenses: List<AcceptedGroupExpenseDto>,
)

data class AcceptedGroupExpenseDto(
    val creatorId: String,
    val title: String,
    val totalCost: BigDecimal,
    val baseCurrency: String,
    val targetCurrency: String?,
    val exchangeRate: BigDecimal?,
    val participants: List<AcceptedGroupExpenseParticipantDto>,
    val expenseDate: Instant,
)
fun Expense.toAcceptedGroupExpenseDto() = AcceptedGroupExpenseDto(
    creatorId = creatorId,
    title = title,
    totalCost = totalCost,
    baseCurrency = baseCurrency,
    targetCurrency = targetCurrency,
    exchangeRate = exchangeRate?.value,
    participants = expenseParticipants.map { it.toAcceptedGroupExpenseParticipantDto() },
    expenseDate = expenseDate,
)

data class AcceptedGroupExpenseParticipantDto(
    val participantId: String,
    val participantCost: BigDecimal,
)
private fun ExpenseParticipant.toAcceptedGroupExpenseParticipantDto() =
    AcceptedGroupExpenseParticipantDto(
        participantId = participantId,
        participantCost = participantCost,
    )

fun List<Expense>.toAcceptedGroupExpensesResponse(groupId: String) = AcceptedGroupExpensesResponse(
    groupId = groupId,
    expenses = map { it.toAcceptedGroupExpenseDto() },
)
