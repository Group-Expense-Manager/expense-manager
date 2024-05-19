package pl.edu.agh.gem.internal.validation

import pl.edu.agh.gem.internal.validation.ValidationMessage.COST_NOT_SUM_UP
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class CostValidator : BaseValidator<ExpenseDataWrapper>() {
    override val checks: List<Check<ExpenseDataWrapper>> = listOf(
        Check(COST_NOT_SUM_UP) { this.validateCosts(it) },
    )

    private fun validateCosts(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        return expenseDataWrapper.expense.cost == expenseDataWrapper.expense.expenseParticipants.sumOf { it.participantCost }
    }
}
