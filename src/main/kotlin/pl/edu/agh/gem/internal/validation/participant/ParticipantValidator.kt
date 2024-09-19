package pl.edu.agh.gem.internal.validation.participant

import pl.edu.agh.gem.internal.validation.ValidationMessage.CREATOR_IN_PARTICIPANTS
import pl.edu.agh.gem.internal.validation.ValidationMessage.DUPLICATED_PARTICIPANT
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_NOT_GROUP_MEMBER
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class ParticipantValidator : BaseValidator<ParticipantDataWrapper>() {
    override val checks: List<Check<ParticipantDataWrapper>> = listOf(
        Check(CREATOR_IN_PARTICIPANTS) { this.validateIfUserIsNotParticipant(it) },
        Check(DUPLICATED_PARTICIPANT) { this.validateIfParticipantsAreUnique(it) },
        Check(PARTICIPANT_NOT_GROUP_MEMBER) { this.validateIfParticipantsAreGroupMembers(it) },
    )

    private fun validateIfUserIsNotParticipant(participantDataWrapper: ParticipantDataWrapper): Boolean {
        return !participantDataWrapper.participantData.participantsId
            .contains(participantDataWrapper.participantData.creatorId)
    }

    private fun validateIfParticipantsAreUnique(participantDataWrapper: ParticipantDataWrapper): Boolean {
        return participantDataWrapper.participantData.participantsId.distinctBy { it }.size ==
            participantDataWrapper.participantData.participantsId.size
    }

    private fun validateIfParticipantsAreGroupMembers(participantDataWrapper: ParticipantDataWrapper): Boolean {
        val membersIds = participantDataWrapper.participantData.groupMembers.members.map { it.id }
        return participantDataWrapper.participantData.participantsId
            .filterNot { it in membersIds }
            .isEmpty()
    }
}
