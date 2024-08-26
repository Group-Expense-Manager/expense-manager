package pl.edu.agh.gem.internal.validation.update

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseUpdate
import pl.edu.agh.gem.internal.model.expense.toExpenseParticipantCost
import pl.edu.agh.gem.internal.validation.ValidationMessage.NO_MODIFICATION
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class ModificationValidator : BaseValidator<ExpenseUpdateDataWrapper>() {
    override val checks: List<Check<ExpenseUpdateDataWrapper>> = listOf(
        Check(NO_MODIFICATION) { validateRecipient(it) },
    )

    private fun validateRecipient(updateDataWrapper: ExpenseUpdateDataWrapper): Boolean {
        return updateDataWrapper.expenseUpdate.modifies(updateDataWrapper.originalExpense)
    }

    private fun ExpenseUpdate.modifies(expense: Expense): Boolean {
        return expense.title != this.title ||
            expense.cost != this.cost ||
            expense.baseCurrency != this.baseCurrency ||
            expense.targetCurrency != this.targetCurrency ||
            expense.expenseDate != this.expenseDate ||
            expense.expenseParticipants.map { it.toExpenseParticipantCost() }.toSet() != this.expenseParticipantsCost.toSet()
    }
}
