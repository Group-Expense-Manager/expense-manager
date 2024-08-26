package pl.edu.agh.gem.internal.validation.cost

import java.math.BigDecimal

data class CostData(
    val fullCost: BigDecimal,
    val partialCosts: List<BigDecimal>,
)
