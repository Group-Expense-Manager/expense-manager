package pl.edu.agh.gem.integration.persistence

import io.kotest.matchers.nulls.shouldBeNull
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.util.createExpense

class MongoExpenseRepositoryIT(
    private val expenseRepository: ExpenseRepository,
) : BaseIntegrationSpec({

    should("delete expense") {
        // given
        val expense = createExpense()
        expenseRepository.save(expense)

        // when
        expenseRepository.delete(expense)

        // then
        expenseRepository.findByExpenseIdAndGroupId(expense.id, expense.groupId).also {
            it.shouldBeNull()
        }
    }
},)
