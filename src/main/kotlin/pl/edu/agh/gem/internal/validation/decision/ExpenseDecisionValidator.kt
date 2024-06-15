package pl.edu.agh.gem.internal.validation.decision

import pl.edu.agh.gem.internal.validation.ValidationMessage.CREATOR_DECISION
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class ExpenseDecisionValidator : BaseValidator<ExpenseDecisionDataWrapper>() {
    override val checks: List<Check<ExpenseDecisionDataWrapper>> = listOf(
        Check(CREATOR_DECISION) { validateCreator(it) },
    )

    private fun validateCreator(expenseDecisionDataWrapper: ExpenseDecisionDataWrapper): Boolean {
        return expenseDecisionDataWrapper.expenseDecision.userId != expenseDecisionDataWrapper.expense.creatorId
    }
}
