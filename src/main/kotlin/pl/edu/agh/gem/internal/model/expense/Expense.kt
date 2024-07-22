package pl.edu.agh.gem.internal.model.expense

import pl.edu.agh.gem.internal.model.currency.ExchangeRate
import java.math.BigDecimal
import java.time.Instant

data class Expense(
    val id: String,
    val groupId: String,
    val creatorId: String,
    val title: String,
    val cost: BigDecimal,
    val baseCurrency: String,
    val targetCurrency: String?,
    val exchangeRate: ExchangeRate?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val expenseDate: Instant,
    val attachmentId: String,
    val expenseParticipants: List<ExpenseParticipant>,
    val status: ExpenseStatus,
    val statusHistory: List<StatusHistoryEntry>,
)
