package pl.edu.agh.gem.external.persistence

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.where
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.persistence.ExpenseRepository

@Repository
class MongoExpenseRepository(
    private val mongo: MongoTemplate,
) : ExpenseRepository {
    override fun create(expense: Expense): Expense {
        return mongo.insert(expense.toEntity()).toDomain()
    }

    override fun findByExpenseIdAndGroupId(expenseId: String, groupId: String): Expense? {
        val query = Query()
            .addCriteria(where(ExpenseEntity::id).isEqualTo(expenseId))
            .addCriteria(where(ExpenseEntity::groupId).isEqualTo(groupId))
        return mongo.findOne(query, ExpenseEntity::class.java)?.toDomain()
    }

    private fun Expense.toEntity() =
        ExpenseEntity(
            id = id,
            groupId = groupId,
            creatorId = creatorId,
            title = title,
            cost = cost,
            baseCurrency = baseCurrency,
            targetCurrency = targetCurrency,
            exchangeRate = exchangeRate?.value,
            createdAt = createdAt,
            updatedAt = updatedAt,
            expenseDate = expenseDate,
            attachmentId = attachmentId,
            expenseParticipants = expenseParticipants,
            status = status,
            statusHistory = statusHistory,
        )
}
