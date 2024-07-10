package pl.edu.agh.gem.external.dto.expense

import pl.edu.agh.gem.internal.model.expense.Expense
import java.math.BigDecimal
import java.time.Instant

data class ExternalGroupExpensesResponse(
    val expenses: List<ExternalGroupExpensesDto>,
)

data class ExternalGroupExpensesDto(
    val expenseId: String,
    val creatorId: String,
    val title: String,
    val cost: BigDecimal,
    val baseCurrency: String,
    val status: String,
    val participantIds: List<String>,
    val expenseDate: Instant,
) {
    companion object {
        fun fromExpense(expense: Expense) = ExternalGroupExpensesDto(
            expenseId = expense.id,
            creatorId = expense.creatorId,
            title = expense.title,
            cost = expense.cost,
            baseCurrency = expense.baseCurrency,
            status = expense.status.name,
            participantIds = expense.expenseParticipants.map { it.participantId },
            expenseDate = expense.expenseDate,
        )
    }
}

fun List<Expense>.toExternalGroupExpensesResponse() = ExternalGroupExpensesResponse(
    expenses = map { ExternalGroupExpensesDto.fromExpense(it) },
)
