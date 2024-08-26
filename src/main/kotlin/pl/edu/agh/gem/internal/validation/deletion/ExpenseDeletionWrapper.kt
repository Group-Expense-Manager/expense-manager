package pl.edu.agh.gem.internal.validation.deletion

import pl.edu.agh.gem.internal.validation.creator.CreatorData
import pl.edu.agh.gem.internal.validation.creator.CreatorDataWrapper

data class ExpenseDeletionWrapper(override val creatorData: CreatorData) : CreatorDataWrapper
