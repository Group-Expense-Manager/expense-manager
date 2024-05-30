package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.expense.Expense

interface ExpenseRepository {
    fun create(expense: Expense): Expense
}
