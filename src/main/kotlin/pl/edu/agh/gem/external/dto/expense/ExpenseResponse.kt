package pl.edu.agh.gem.external.dto.expense

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseHistoryEntry
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipant
import java.math.BigDecimal
import java.time.Instant

data class ExpenseResponse(
    val expenseId: String,
    val creatorId: String,
    val title: String,
    val cost: BigDecimal,
    val baseCurrency: String,
    val targetCurrency: String?,
    val exchangeRate: BigDecimal?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val expenseDate: Instant,
    val attachmentId: String?,
    val expenseParticipants: List<ExpenseParticipantResponseData>,
    val status: String,
    val history: List<ExpenseHistoryEntryResponseData>,
) {
    companion object {
        fun fromExpense(expense: Expense) = ExpenseResponse(
            expenseId = expense.id,
            creatorId = expense.creatorId,
            title = expense.title,
            cost = expense.cost,
            baseCurrency = expense.baseCurrency,
            targetCurrency = expense.targetCurrency,
            exchangeRate = expense.exchangeRate?.value,
            createdAt = expense.createdAt,
            updatedAt = expense.updatedAt,
            expenseDate = expense.expenseDate,
            attachmentId = expense.attachmentId,
            expenseParticipants = expense.expenseParticipants.map { ExpenseParticipantResponseData.fromExpenseParticipant(it) },
            status = expense.status.name,
            history = expense.history.map { ExpenseHistoryEntryResponseData.fromExpenseHistoryEntry(it) },
        )
    }
}

data class ExpenseParticipantResponseData(
    val participantId: String,
    val participantCost: BigDecimal,
    val participantStatus: String,
) {
    companion object {
        fun fromExpenseParticipant(expenseParticipant: ExpenseParticipant) = ExpenseParticipantResponseData(
            participantId = expenseParticipant.participantId,
            participantCost = expenseParticipant.participantCost,
            participantStatus = expenseParticipant.participantStatus.name,
        )
    }
}

data class ExpenseHistoryEntryResponseData(
    val participantId: String,
    val expenseAction: String,
    val createdAt: Instant,
    val comment: String?,
) {
    companion object {
        fun fromExpenseHistoryEntry(expenseHistoryEntry: ExpenseHistoryEntry) = ExpenseHistoryEntryResponseData(
            participantId = expenseHistoryEntry.participantId,
            expenseAction = expenseHistoryEntry.expenseAction.name,
            createdAt = expenseHistoryEntry.createdAt,
            comment = expenseHistoryEntry.comment,
        )
    }
}
