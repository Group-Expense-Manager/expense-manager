package pl.edu.agh.gem.internal.model.expense

import pl.edu.agh.gem.internal.model.currency.ExchangeRate
import pl.edu.agh.gem.internal.model.expense.ExpenseAction.CREATED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import java.math.BigDecimal
import java.time.Instant
import java.time.Instant.now
import java.util.UUID.randomUUID

data class ExpenseCreation(
    val groupId: String,
    val creatorId: String,
    val title: String,
    val cost: BigDecimal,
    val baseCurrency: String,
    val targetCurrency: String?,
    val expenseDate: Instant,
    val message: String? = null,
    val expenseParticipantsCost: List<ExpenseParticipantCost>,
    val attachmentId: String?,
) {
    fun toExpense(exchangeRate: ExchangeRate?, attachmentId: String) = Expense(
        id = randomUUID().toString(),
        groupId = groupId,
        creatorId = creatorId,
        title = title,
        cost = cost,
        baseCurrency = baseCurrency,
        targetCurrency = targetCurrency,
        exchangeRate = exchangeRate,
        expenseDate = expenseDate,
        createdAt = now(),
        updatedAt = now(),
        expenseParticipants = expenseParticipantsCost.map { it.toExpenseParticipant(creatorId) },
        attachmentId = attachmentId,
        status = PENDING,
        history = arrayListOf(ExpenseHistoryEntry(creatorId, CREATED, comment = message)),

    )
}
