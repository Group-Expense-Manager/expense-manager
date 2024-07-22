package pl.edu.agh.gem.external.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.model.currency.ExchangeRate
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipant
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.StatusHistoryEntry
import java.math.BigDecimal
import java.time.Instant

@Document("expenses")
data class ExpenseEntity(
    @Id
    val id: String,
    val groupId: String,
    val creatorId: String,
    val title: String,
    val cost: BigDecimal,
    val baseCurrency: String,
    val targetCurrency: String?,
    val exchangeRate: BigDecimal?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val expenseDate: Instant,
    val attachmentId: String,
    val expenseParticipants: List<ExpenseParticipant>,
    val status: ExpenseStatus,
    val statusHistory: List<StatusHistoryEntry>,
) {
    fun toDomain() =
        Expense(
            id = id,
            groupId = groupId,
            creatorId = creatorId,
            title = title,
            cost = cost,
            baseCurrency = baseCurrency,
            targetCurrency = targetCurrency,
            exchangeRate = exchangeRate?.let { ExchangeRate(it) },
            createdAt = createdAt,
            updatedAt = updatedAt,
            expenseDate = expenseDate,
            attachmentId = attachmentId,
            expenseParticipants = expenseParticipants,
            status = status,
            statusHistory = statusHistory,
        )
}
