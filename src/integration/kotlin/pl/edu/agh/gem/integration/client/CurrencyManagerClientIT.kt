package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import pl.edu.agh.gem.external.dto.currency.CurrenciesResponse
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubCurrencyManagerAvailableCurrencies
import pl.edu.agh.gem.integration.ability.stubCurrencyManagerExchangeRate
import pl.edu.agh.gem.internal.client.CurrencyManagerClient
import pl.edu.agh.gem.internal.client.CurrencyManagerClientException
import pl.edu.agh.gem.internal.client.RetryableCurrencyManagerClientException
import pl.edu.agh.gem.internal.model.currency.Currency
import pl.edu.agh.gem.util.DummyData.CURRENCY_1
import pl.edu.agh.gem.util.DummyData.CURRENCY_2
import pl.edu.agh.gem.util.DummyData.EXCHANGE_RATE_VALUE
import pl.edu.agh.gem.util.createCurrenciesResponse
import pl.edu.agh.gem.util.createExchangeRateResponse
import java.time.Instant

class CurrencyManagerClientIT(
    private val currencyManagerClient: CurrencyManagerClient,
) : BaseIntegrationSpec({
    should("get currencies") {
        // given
        val listOfCurrencies = listOf("PLN", "USD", "EUR")
        val currenciesResponse = CurrenciesResponse(listOfCurrencies.map { Currency(it) })
        stubCurrencyManagerAvailableCurrencies(currenciesResponse)

        // when
        val result = currencyManagerClient.getAvailableCurrencies()

        // then
        result.currencies.all {
            it.code in listOfCurrencies
        }
    }

    should("throw CurrencyManagerClientException when we send bad request") {
        // given
        val currenciesResponse = createCurrenciesResponse()
        stubCurrencyManagerAvailableCurrencies(currenciesResponse, NOT_ACCEPTABLE)

        // when & then
        shouldThrow<CurrencyManagerClientException> {
            currencyManagerClient.getAvailableCurrencies()
        }
    }

    should("throw RetryableCurrencyManagerClientException when client has internal error") {
        // given
        val currenciesResponse = createCurrenciesResponse()
        stubCurrencyManagerAvailableCurrencies(currenciesResponse, INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableCurrencyManagerClientException> {
            currencyManagerClient.getAvailableCurrencies()
        }
    }

    should("get exchange rate") {
        // given
        val exchangeRateResponse = createExchangeRateResponse(EXCHANGE_RATE_VALUE)
        stubCurrencyManagerExchangeRate(exchangeRateResponse, CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))

        // when
        val result = currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))

        // then
        result.value shouldBe EXCHANGE_RATE_VALUE
    }

    should("throw CurrencyManagerClientException when we send bad request") {
        // given
        val exchangeRateResponse = createExchangeRateResponse()
        stubCurrencyManagerExchangeRate(exchangeRateResponse, CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L), NOT_ACCEPTABLE)

        // when & then
        shouldThrow<CurrencyManagerClientException> {
            currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))
        }
    }

    should("throw RetryableCurrencyManagerClientException when client has internal error") {
        // given
        val exchangeRateResponse = createExchangeRateResponse()
        stubCurrencyManagerExchangeRate(exchangeRateResponse, CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L), INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableCurrencyManagerClientException> {
            currencyManagerClient.getExchangeRate(CURRENCY_1, CURRENCY_2, Instant.ofEpochMilli(0L))
        }
    }
},)
