package pl.edu.agh.gem.internal.model.expense

import pl.edu.agh.gem.internal.model.currency.ExchangeRate
import java.math.BigDecimal

data class UserExpense(
    val value: BigDecimal,
    val currency: String,
    val exchangeRate: ExchangeRate?,
)
