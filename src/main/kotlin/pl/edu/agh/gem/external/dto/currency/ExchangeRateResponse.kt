package pl.edu.agh.gem.external.dto.currency

import pl.edu.agh.gem.internal.model.currency.ExchangeRate
import java.math.BigDecimal
import java.time.Instant

data class ExchangeRateResponse(
    val currencyFrom: String,
    val currencyTo: String,
    val rate: BigDecimal,
    val createdAt: Instant,
) {
    fun toDomain() = ExchangeRate(
        value = rate,
    )
}
