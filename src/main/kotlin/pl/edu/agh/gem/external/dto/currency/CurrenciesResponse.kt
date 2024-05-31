package pl.edu.agh.gem.external.dto.currency

import pl.edu.agh.gem.external.dto.group.CurrencyDTO
import pl.edu.agh.gem.internal.model.currency.Currency

data class CurrenciesResponse(
    val currencies: List<CurrencyDTO>,
) {
    fun toDomain() = currencies.map { Currency(it.code) }
}
