package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.group.Currencies
import java.math.BigDecimal
import java.time.LocalDate

interface CurrencyManagerClient {
    fun getAvailableCurrencies(): Currencies

    fun getExchangeRate(
        baseCurrency: String,
        targetCurrency: String,
        date: LocalDate,
    ): BigDecimal
}

class CurrencyManagerClientException(override val message: String?) : RuntimeException()

class RetryableCurrencyManagerClientException(override val message: String?) : RuntimeException()
