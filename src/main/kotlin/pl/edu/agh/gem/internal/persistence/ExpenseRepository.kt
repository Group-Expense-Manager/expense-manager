package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.expense.Expense

interface ExpenseRepository {
    fun create(expense: Expense): Expense
    fun findByExpenseIdAndGroupId(expenseId: String, groupId: String): Expense?
    fun findByGroupId(groupId: String): List<Expense>
}
