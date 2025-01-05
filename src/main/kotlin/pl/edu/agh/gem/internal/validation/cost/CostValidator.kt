package pl.edu.agh.gem.internal.validation.cost

import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_COSTS_HIGHER_THAN_TOTAL_COST
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class CostValidator : BaseValidator<CostDataWrapper>() {
    override val checks: List<Check<CostDataWrapper>> =
        listOf(
            Check(PARTICIPANT_COSTS_HIGHER_THAN_TOTAL_COST) { this.validateCosts(it) },
        )

    private fun validateCosts(costDataWrapper: CostDataWrapper): Boolean {
        return costDataWrapper.costData.fullCost >= costDataWrapper.costData.partialCosts.sumOf { it }
    }
}
