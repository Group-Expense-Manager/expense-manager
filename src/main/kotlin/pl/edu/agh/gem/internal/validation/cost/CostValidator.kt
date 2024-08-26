package pl.edu.agh.gem.internal.validation.cost

import pl.edu.agh.gem.internal.validation.ValidationMessage.COST_NOT_SUM_UP
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class CostValidator : BaseValidator<CostDataWrapper>() {
    override val checks: List<Check<CostDataWrapper>> = listOf(
        Check(COST_NOT_SUM_UP) { this.validateCosts(it) },
    )

    private fun validateCosts(costDataWrapper: CostDataWrapper): Boolean {
        return costDataWrapper.costData.fullCost == costDataWrapper.costData.partialCosts.sumOf { it }
    }
}
