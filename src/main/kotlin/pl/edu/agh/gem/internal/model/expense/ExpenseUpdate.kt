package pl.edu.agh.gem.internal.model.expense

import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import java.math.BigDecimal
import java.time.Instant

data class ExpenseUpdate(
    val id: String,
    val groupId: String,
    val userId: String,
    val title: String,
    val cost: BigDecimal,
    val baseCurrency: String,
    val targetCurrency: String?,
    val expenseDate: Instant,
    val expenseParticipantsCost: List<ExpenseParticipantCost>,
    val message: String?,
)

data class ExpenseParticipantCost(
    val participantId: String,
    val participantCost: BigDecimal,
) {
    fun toExpenseParticipant(creatorId: String) = ExpenseParticipant(
        participantId = participantId,
        participantCost = participantCost,
        participantStatus = if (creatorId == participantId) ACCEPTED else PENDING,
    )
}
fun ExpenseParticipant.toExpenseParticipantCost() =
    ExpenseParticipantCost(
        participantId = participantId,
        participantCost = participantCost,
    )
