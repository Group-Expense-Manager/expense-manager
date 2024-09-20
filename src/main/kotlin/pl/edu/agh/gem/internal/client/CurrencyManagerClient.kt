package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.currency.ExchangeRate
import pl.edu.agh.gem.internal.model.group.Currencies
import java.time.LocalDate

interface CurrencyManagerClient {
    fun getAvailableCurrencies(): Currencies
    fun getExchangeRate(baseCurrency: String, targetCurrency: String, date: LocalDate): ExchangeRate
}

class CurrencyManagerClientException(override val message: String?) : RuntimeException()

class RetryableCurrencyManagerClientException(override val message: String?) : RuntimeException()
