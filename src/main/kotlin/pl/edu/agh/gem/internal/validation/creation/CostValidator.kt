package pl.edu.agh.gem.internal.validation.creation

import pl.edu.agh.gem.internal.validation.ValidationMessage.COST_NOT_SUM_UP
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class CostValidator : BaseValidator<ExpenseCreationDataWrapper>() {
    override val checks: List<Check<ExpenseCreationDataWrapper>> = listOf(
        Check(COST_NOT_SUM_UP) { this.validateCosts(it) },
    )

    private fun validateCosts(expenseCreationDataWrapper: ExpenseCreationDataWrapper): Boolean {
        return expenseCreationDataWrapper.expense.cost == expenseCreationDataWrapper.expense.expenseParticipants.sumOf { it.participantCost }
    }
}
