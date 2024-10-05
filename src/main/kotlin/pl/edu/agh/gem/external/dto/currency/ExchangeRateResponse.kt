package pl.edu.agh.gem.external.dto.currency

import java.math.BigDecimal
import java.time.Instant

data class ExchangeRateResponse(
    val currencyFrom: String,
    val currencyTo: String,
    val rate: BigDecimal,
    val createdAt: Instant,
) {
    fun toDomain() = rate
}
