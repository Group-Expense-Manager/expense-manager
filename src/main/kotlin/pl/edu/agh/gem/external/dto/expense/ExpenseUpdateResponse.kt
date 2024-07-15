package pl.edu.agh.gem.external.dto.expense

import pl.edu.agh.gem.internal.model.expense.Expense

data class ExpenseUpdateResponse(
    val expenseId: String,
)

fun Expense.toExpenseUpdateResponse() = ExpenseUpdateResponse(id)
