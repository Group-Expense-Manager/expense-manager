package pl.edu.agh.gem.internal.validation.creator

import pl.edu.agh.gem.validator.DataWrapper

interface CreatorDataWrapper : DataWrapper {
    val creatorData: CreatorData
}
