package pl.edu.agh.gem.internal.model.expense

import java.math.BigDecimal

data class ExpenseParticipant(
    val participantId: String,
    val participantCost: BigDecimal,
    val participantStatus: ExpenseStatus,
)
