package pl.edu.agh.gem.internal.model.expense

import pl.edu.agh.gem.external.dto.expense.ExpenseParticipantRequestData
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
    val expenseParticipants: List<ExpenseUpdateParticipant>,
    val message: String?,
)

data class ExpenseUpdateParticipant(
    val participantId: String,
    val participantCost: BigDecimal,
) {
    fun toExpenseParticipant() = ExpenseParticipant(
        participantId = participantId,
        participantCost = participantCost,
        participantStatus = PENDING,
    )
}
fun ExpenseParticipant.toExpenseUpdateParticipant() =
    ExpenseUpdateParticipant(
        participantId = participantId,
        participantCost = participantCost,
    )

fun ExpenseParticipantRequestData.toExpenseUpdateParticipant() = ExpenseUpdateParticipant(
    participantId = participantId,
    participantCost = participantCost,
)
