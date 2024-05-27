package pl.edu.agh.gem.internal.validation.creation

import pl.edu.agh.gem.internal.validation.ValidationMessage.DUPLICATED_PARTICIPANT
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_MIN_SIZE
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_NOT_GROUP_MEMBER
import pl.edu.agh.gem.internal.validation.ValidationMessage.USER_NOT_PARTICIPANT
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class ParticipantValidator : BaseValidator<ExpenseCreationDataWrapper>() {
    override val checks: List<Check<ExpenseCreationDataWrapper>> = listOf(
        Check(USER_NOT_PARTICIPANT) { this.validateIfUserIsParticipant(it) },
        Check(DUPLICATED_PARTICIPANT) { this.validateIfParticipantsAreUnique(it) },
        Check(PARTICIPANT_MIN_SIZE) { this.validateParticipantsSize(it) },
        Check(PARTICIPANT_NOT_GROUP_MEMBER) { this.validateIfParticipantsAreGroupMembers(it) },
    )

    private fun validateIfUserIsParticipant(expenseCreationDataWrapper: ExpenseCreationDataWrapper): Boolean {
        return expenseCreationDataWrapper.expense.expenseParticipants
            .map { it.participantId }
            .contains(expenseCreationDataWrapper.expense.creatorId)
    }

    private fun validateIfParticipantsAreUnique(expenseCreationDataWrapper: ExpenseCreationDataWrapper): Boolean {
        return expenseCreationDataWrapper.expense.expenseParticipants.distinctBy { it.participantId }.size ==
            expenseCreationDataWrapper.expense.expenseParticipants.size
    }

    private fun validateParticipantsSize(expenseCreationDataWrapper: ExpenseCreationDataWrapper): Boolean {
        return expenseCreationDataWrapper.expense.expenseParticipants.size > 1
    }

    private fun validateIfParticipantsAreGroupMembers(expenseCreationDataWrapper: ExpenseCreationDataWrapper): Boolean {
        val membersIds = expenseCreationDataWrapper.group.members.members.map { it.id }
        return expenseCreationDataWrapper.expense.expenseParticipants
            .map { it.participantId }
            .filterNot { it in membersIds }
            .isEmpty()
    }
}
