package pl.edu.agh.gem.internal.validation.decision

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseDecision
import pl.edu.agh.gem.validator.DataWrapper

data class ExpenseDecisionDataWrapper(
    val expense: Expense,
    val expenseDecision: ExpenseDecision,
) : DataWrapper
