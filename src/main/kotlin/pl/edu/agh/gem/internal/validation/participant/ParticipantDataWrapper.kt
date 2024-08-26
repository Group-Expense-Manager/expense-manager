package pl.edu.agh.gem.internal.validation.participant

import pl.edu.agh.gem.validator.DataWrapper

interface ParticipantDataWrapper : DataWrapper {
    val participantData: ParticipantData
}
