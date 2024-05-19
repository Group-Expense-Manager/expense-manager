package pl.edu.agh.gem.external.dto.currency

import pl.edu.agh.gem.internal.model.currency.Currencies
import pl.edu.agh.gem.internal.model.currency.Currency

data class CurrenciesResponse(
    val currencies: List<Currency>,
) {
    fun toDomain() = Currencies(
        currencies = currencies,
    )
}
