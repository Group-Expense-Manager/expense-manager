package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.filter.FilterOptions

interface ExpenseRepository {
    fun save(expense: Expense): Expense
    fun findByExpenseIdAndGroupId(expenseId: String, groupId: String): Expense?
    fun findByGroupId(groupId: String, filterOptions: FilterOptions? = null): List<Expense>
    fun delete(expense: Expense)
}
