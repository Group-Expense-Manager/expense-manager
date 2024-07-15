package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.expense.Expense

interface ExpenseRepository {
    fun save(expense: Expense): Expense
    fun findByExpenseIdAndGroupId(expenseId: String, groupId: String): Expense?
    fun findByGroupId(groupId: String): List<Expense>
    fun delete(expense: Expense)
}
