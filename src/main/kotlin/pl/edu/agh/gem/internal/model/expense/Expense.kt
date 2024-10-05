package pl.edu.agh.gem.internal.model.expense

import java.math.BigDecimal
import java.time.Instant

data class Expense(
    val id: String,
    val groupId: String,
    val creatorId: String,
    val title: String,
    val amount: Amount,
    val fxData: FxData?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val expenseDate: Instant,
    val attachmentId: String?,
    val expenseParticipants: List<ExpenseParticipant>,
    val status: ExpenseStatus,
    val history: List<ExpenseHistoryEntry>,
)

data class Amount(
    val value: BigDecimal,
    val currency: String,
)

data class FxData(
    val targetCurrency: String,
    val exchangeRate: BigDecimal,
)
