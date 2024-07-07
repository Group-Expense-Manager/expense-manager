package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.expense.Expense

interface ArchivedExpenseRepository {
    fun add(expense: Expense)
}
