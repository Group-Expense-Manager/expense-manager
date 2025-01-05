package pl.edu.agh.gem.internal.validation.creator

import pl.edu.agh.gem.internal.validation.ValidationMessage.USER_NOT_CREATOR
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class CreatorValidator : BaseValidator<CreatorDataWrapper>() {
    override val checks: List<Check<CreatorDataWrapper>> =
        listOf(
            Check(USER_NOT_CREATOR) { validateRecipient(it) },
        )

    private fun validateRecipient(creatorDataWrapper: CreatorDataWrapper): Boolean {
        return creatorDataWrapper.creatorData.userId == creatorDataWrapper.creatorData.creatorId
    }
}
