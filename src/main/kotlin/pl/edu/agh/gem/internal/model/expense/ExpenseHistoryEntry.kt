package pl.edu.agh.gem.internal.model.expense

import java.time.Instant
import java.time.Instant.now

data class ExpenseHistoryEntry(
    val participantId: String,
    val expenseAction: ExpenseAction,
    val createdAt: Instant = now(),
    val comment: String? = null,
)
