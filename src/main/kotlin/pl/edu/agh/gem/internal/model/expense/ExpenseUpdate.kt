package pl.edu.agh.gem.internal.model.expense

import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import java.math.BigDecimal
import java.time.Instant

data class ExpenseUpdate(
    val id: String,
    val groupId: String,
    val userId: String,
    val title: String,
    val amount: Amount,
    val targetCurrency: String?,
    val expenseDate: Instant,
    val expenseParticipantsCost: List<ExpenseParticipantCost>,
    val message: String?,
    val attachmentId: String?,
)

data class ExpenseParticipantCost(
    val participantId: String,
    val participantCost: BigDecimal,
) {
    fun toExpenseParticipant() =
        ExpenseParticipant(
            participantId = participantId,
            participantCost = participantCost,
            participantStatus = PENDING,
        )
}

fun ExpenseParticipant.toExpenseParticipantCost() =
    ExpenseParticipantCost(
        participantId = participantId,
        participantCost = participantCost,
    )
