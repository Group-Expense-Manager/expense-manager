package pl.edu.agh.gem.internal.model.expense

import java.math.BigDecimal

data class UserExpense(
    val value: BigDecimal,
    val currency: String,
    val exchangeRate: BigDecimal?,
)
