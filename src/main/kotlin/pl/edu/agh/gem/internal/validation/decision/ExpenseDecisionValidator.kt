package pl.edu.agh.gem.internal.validation.decision

import pl.edu.agh.gem.internal.validation.ValidationMessage.USER_NOT_PARTICIPANT
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class ExpenseDecisionValidator : BaseValidator<ExpenseDecisionDataWrapper>() {
    override val checks: List<Check<ExpenseDecisionDataWrapper>> =
        listOf(
            Check(USER_NOT_PARTICIPANT) { validateParticipant(it) },
        )

    private fun validateParticipant(expenseDecisionDataWrapper: ExpenseDecisionDataWrapper): Boolean {
        return expenseDecisionDataWrapper.expense.expenseParticipants
            .map { it.participantId }
            .contains(expenseDecisionDataWrapper.expenseDecision.userId)
    }
}
