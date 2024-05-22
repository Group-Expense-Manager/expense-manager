package pl.edu.agh.gem.internal.validation

import pl.edu.agh.gem.internal.validation.ValidationMessage.DUPLICATED_PARTICIPANT
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_MIN_SIZE
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_NOT_GROUP_MEMBER
import pl.edu.agh.gem.internal.validation.ValidationMessage.USER_NOT_PARTICIPANT
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class ParticipantValidator : BaseValidator<ExpenseDataWrapper>() {
    override val checks: List<Check<ExpenseDataWrapper>> = listOf(
        Check(USER_NOT_PARTICIPANT) { this.validateIfUserIsParticipant(it) },
        Check(DUPLICATED_PARTICIPANT) { this.validateIfParticipantsAreUnique(it) },
        Check(PARTICIPANT_MIN_SIZE) { this.validateParticipantsSize(it) },
        Check(PARTICIPANT_NOT_GROUP_MEMBER) { this.validateIfParticipantsAreGroupMembers(it) },
    )

    private fun validateIfUserIsParticipant(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        return expenseDataWrapper.expense.expenseParticipants
            .map { it.participantId }
            .contains(expenseDataWrapper.expense.creatorId)
    }

    private fun validateIfParticipantsAreUnique(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        return expenseDataWrapper.expense.expenseParticipants.distinctBy { it.participantId }.size ==
            expenseDataWrapper.expense.expenseParticipants.size
    }

    private fun validateParticipantsSize(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        return expenseDataWrapper.expense.expenseParticipants.size > 1
    }

    private fun validateIfParticipantsAreGroupMembers(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        val membersIds = expenseDataWrapper.groupMembers.members.map { it.id }
        return expenseDataWrapper.expense.expenseParticipants
            .map { it.participantId }
            .filterNot { it in membersIds }
            .isEmpty()
    }
}
