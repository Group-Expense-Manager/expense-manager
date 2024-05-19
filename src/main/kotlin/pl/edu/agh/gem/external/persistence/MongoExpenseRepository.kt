package pl.edu.agh.gem.external.persistence

import org.springframework.data.mongodb.core.MongoTemplate
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
