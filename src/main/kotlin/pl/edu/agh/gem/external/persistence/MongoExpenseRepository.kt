package pl.edu.agh.gem.external.persistence

import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.where
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.filter.FilterOptions
import pl.edu.agh.gem.internal.model.expense.filter.SortOrder.ASCENDING
import pl.edu.agh.gem.internal.model.expense.filter.SortOrder.DESCENDING
import pl.edu.agh.gem.internal.model.expense.filter.SortedBy.DATE
import pl.edu.agh.gem.internal.model.expense.filter.SortedBy.TITLE
import pl.edu.agh.gem.internal.persistence.ExpenseRepository

@Repository
class MongoExpenseRepository(
    private val mongo: MongoTemplate,
) : ExpenseRepository {
    override fun save(expense: Expense): Expense {
        return mongo.save(expense.toEntity()).toDomain()
    }

    override fun findByExpenseIdAndGroupId(expenseId: String, groupId: String): Expense? {
        val query = Query()
            .addCriteria(where(ExpenseEntity::id).isEqualTo(expenseId))
            .addCriteria(where(ExpenseEntity::groupId).isEqualTo(groupId))
        return mongo.findOne(query, ExpenseEntity::class.java)?.toDomain()
    }

    override fun findByGroupId(groupId: String, filterOptions: FilterOptions?): List<Expense> {
        val query = Query().addCriteria(where(ExpenseEntity::groupId).isEqualTo(groupId))

        filterOptions?.also {
            it.title?.also { title ->
                val regex = ".*$title.*"
                query.addCriteria(where(ExpenseEntity::title).regex(regex, "i"))
            }

            it.status?.also { status ->
                query.addCriteria(where(ExpenseEntity::status).isEqualTo(status))
            }

            it.creatorId?.also { creatorId ->
                query.addCriteria(where(ExpenseEntity::creatorId).isEqualTo(creatorId))
            }

            val sortedByField = when (it.sortedBy) {
                TITLE -> ExpenseEntity::title.name
                DATE -> ExpenseEntity::expenseDate.name
            }

            val sort = when (it.sortOrder) {
                ASCENDING -> Sort.by(Sort.Order.asc(sortedByField))
                DESCENDING -> Sort.by(Sort.Order.desc(sortedByField))
            }
            query.with(sort)
        }

        return mongo.find(query, ExpenseEntity::class.java).map(ExpenseEntity::toDomain)
    }

    override fun delete(expense: Expense) {
        mongo.remove(expense.toEntity())
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
            history = history,
        )
}
