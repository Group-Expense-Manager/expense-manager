package pl.edu.agh.gem.internal.model.expense

import pl.edu.agh.gem.internal.model.expense.ExpenseAction.CREATED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import java.time.Instant
import java.time.Instant.now
import java.util.UUID.randomUUID

data class ExpenseCreation(
    val groupId: String,
    val creatorId: String,
    val title: String,
    val amount: Amount,
    val targetCurrency: String?,
    val expenseDate: Instant,
    val message: String? = null,
    val expenseParticipantsCost: List<ExpenseParticipantCost>,
    val attachmentId: String?,
) {
    fun toExpense(fxData: FxData?) = Expense(
        id = randomUUID().toString(),
        groupId = groupId,
        creatorId = creatorId,
        title = title,
        amount = amount,
        fxData = fxData,
        expenseDate = expenseDate,
        createdAt = now(),
        updatedAt = now(),
        expenseParticipants = expenseParticipantsCost.map { it.toExpenseParticipant() },
        attachmentId = attachmentId,
        status = PENDING,
        history = arrayListOf(ExpenseHistoryEntry(creatorId, CREATED, comment = message)),
    )
}
