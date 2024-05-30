package pl.edu.agh.gem.external.dto.currency

import pl.edu.agh.gem.internal.model.currency.ExchangeRate
import java.math.BigDecimal

data class ExchangeRateResponse(
    val value: BigDecimal,
) {
    fun toDomain() = ExchangeRate(
        value = value,
    )
}
