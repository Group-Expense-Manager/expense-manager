package pl.edu.agh.gem.external.persistence

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.persistence.ArchivedExpenseRepository
import pl.edu.agh.gem.metrics.MeteredRepository

@Repository
@MeteredRepository
class MongoArchivedExpenseRepository(
    private val mongo: MongoTemplate,
) : ArchivedExpenseRepository {
    override fun add(expense: Expense) {
        mongo.insert(expense.toEntity())
    }

    private fun Expense.toEntity() =
        ArchivedExpenseEntity(
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
