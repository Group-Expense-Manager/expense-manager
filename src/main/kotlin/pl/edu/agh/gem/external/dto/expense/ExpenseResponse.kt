package pl.edu.agh.gem.external.dto.expense

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipant
import pl.edu.agh.gem.internal.model.expense.StatusHistoryEntry
import java.math.BigDecimal
import java.time.Instant

data class ExpenseResponse(
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
    val statusHistory: List<StatusHistoryEntryResponseData>,
) {
    companion object {
        fun fromExpense(expense: Expense) = ExpenseResponse(
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
            statusHistory = expense.statusHistory.map { StatusHistoryEntryResponseData.fromStatusHistoryEntry(it) },
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

data class StatusHistoryEntryResponseData(
    val participantId: String,
    val expenseAction: String,
    val createdAt: Instant,
    val comment: String?,
) {
    companion object {
        fun fromStatusHistoryEntry(statusHistoryEntry: StatusHistoryEntry) = StatusHistoryEntryResponseData(
            participantId = statusHistoryEntry.participantId,
            expenseAction = statusHistoryEntry.expenseAction.name,
            createdAt = statusHistoryEntry.createdAt,
            comment = statusHistoryEntry.comment,
        )
    }
}
