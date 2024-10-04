package pl.edu.agh.gem.external.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.model.expense.Amount
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseHistoryEntry
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipant
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.FxData
import java.time.Instant

@Document("expenses")
data class ExpenseEntity(
    @Id
    val id: String,
    val groupId: String,
    val creatorId: String,
    @Indexed(background = true)
    val title: String,
    val amount: Amount,
    val fxData: FxData?,
    val createdAt: Instant,
    val updatedAt: Instant,
    @Indexed(background = true)
    val expenseDate: Instant,
    val attachmentId: String?,
    val expenseParticipants: List<ExpenseParticipant>,
    val status: ExpenseStatus,
    val history: List<ExpenseHistoryEntry>,
) {
    fun toDomain() =
        Expense(
            id = id,
            groupId = groupId,
            creatorId = creatorId,
            title = title,
            amount = amount,
            fxData = fxData,
            createdAt = createdAt,
            updatedAt = updatedAt,
            expenseDate = expenseDate,
            attachmentId = attachmentId,
            expenseParticipants = expenseParticipants,
            status = status,
            history = history,
        )
}
