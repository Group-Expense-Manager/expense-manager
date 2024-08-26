package pl.edu.agh.gem.internal.validation.participant

import pl.edu.agh.gem.model.GroupMembers

data class ParticipantData(
    val creatorId: String,
    val participantsId: List<String>,
    val groupMembers: GroupMembers,
)
