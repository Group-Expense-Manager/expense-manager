package pl.edu.agh.gem.external.dto.expense

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import java.math.BigDecimal
import java.time.Instant

data class GroupActivitiesResponse(
    val groupId: String,
    val expenses: List<GroupActivityDTO>,
)

data class GroupActivityDTO(
    val expenseId: String,
    val creatorId: String,
    val title: String,
    val totalCost: BigDecimal,
    val baseCurrency: String,
    val targetCurrency: String?,
    val exchangeRate: BigDecimal?,
    val status: ExpenseStatus,
    val participantIds: List<String>,
    val expenseDate: Instant,
) {
    companion object {
        fun fromExpense(expense: Expense) = GroupActivityDTO(
            expenseId = expense.id,
            creatorId = expense.creatorId,
            title = expense.title,
            totalCost = expense.totalCost,
            baseCurrency = expense.baseCurrency,
            targetCurrency = expense.targetCurrency,
            exchangeRate = expense.exchangeRate?.value,
            status = expense.status,
            participantIds = expense.expenseParticipants.map { it.participantId },
            expenseDate = expense.expenseDate,
        )
    }
}

fun List<Expense>.toGroupActivitiesResponse(groupId: String) = GroupActivitiesResponse(
    groupId = groupId,
    expenses = map { GroupActivityDTO.fromExpense(it) },
)
