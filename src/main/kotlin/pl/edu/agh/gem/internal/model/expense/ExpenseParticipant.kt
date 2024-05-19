package pl.edu.agh.gem.internal.model.expense

import java.math.BigDecimal

data class ExpenseParticipant(
    val participantId: String,
    val participantCost: BigDecimal,
    val participantStatus: ExpenseStatus,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExpenseParticipant) return false
        return participantId == other.participantId
    }

    override fun hashCode(): Int {
        return participantId.hashCode()
    }
}
