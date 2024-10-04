package pl.edu.agh.gem.external.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.model.expense.Amount
import pl.edu.agh.gem.internal.model.expense.ExpenseHistoryEntry
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipant
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.FxData
import java.time.Instant

@Document("archived-expenses")
data class ArchivedExpenseEntity(
    @Id
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
