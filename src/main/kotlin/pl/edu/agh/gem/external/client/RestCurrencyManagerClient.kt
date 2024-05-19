package pl.edu.agh.gem.external.client

import io.github.resilience4j.retry.annotation.Retry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import pl.edu.agh.gem.config.CurrencyManagerProperties
import pl.edu.agh.gem.external.dto.currency.CurrenciesResponse
import pl.edu.agh.gem.external.dto.currency.ExchangeRateResponse
import pl.edu.agh.gem.headers.HeadersUtils.withAppAcceptType
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.internal.client.CurrencyManagerClient
import pl.edu.agh.gem.internal.client.CurrencyManagerClientException
import pl.edu.agh.gem.internal.client.RetryableCurrencyManagerClientException
import pl.edu.agh.gem.internal.model.currency.Currencies
import pl.edu.agh.gem.internal.model.currency.ExchangeRate
import pl.edu.agh.gem.paths.Paths.INTERNAL
import java.time.Instant

@Component
class RestCurrencyManagerClient(
    @Qualifier("CurrencyManagerRestTemplate") val restTemplate: RestTemplate,
    val currencyManagerProperties: CurrencyManagerProperties,
) : CurrencyManagerClient {

    @Retry(name = "currencyManagerClient")
    override fun getAvailableCurrencies(): Currencies {
        return try {
            restTemplate.exchange(
                resolveAvailableCurrenciesAddress(),
                GET,
                HttpEntity<Any>(HttpHeaders().withAppAcceptType().withAppContentType()),
                CurrenciesResponse::class.java,
            ).body?.toDomain() ?: throw CurrencyManagerClientException(
                "While retrieving available currencies using CurrencyManagerClient we received empty body",
            )
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to get available currencies" }
            throw CurrencyManagerClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to get available currencies" }
            throw RetryableCurrencyManagerClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to get available currencies" }
            throw CurrencyManagerClientException(ex.message)
        }
    }

    @Retry(name = "currencyManagerClient")
    override fun getExchangeRate(baseCurrency: String, targetCurrency: String, date: Instant): ExchangeRate {
        return try {
            restTemplate.exchange(
                resolveExchangeRateAddress(baseCurrency, targetCurrency, date),
                GET,
                HttpEntity<Any>(HttpHeaders().withAppAcceptType().withAppContentType()),
                ExchangeRateResponse::class.java,
            ).body?.toDomain() ?: throw CurrencyManagerClientException(
                "While retrieving exchange rate using CurrencyManagerClient we received empty body",
            )
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to get exchange rate" }
            throw CurrencyManagerClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to get exchange rate" }
            throw RetryableCurrencyManagerClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to get exchange rate" }
            throw CurrencyManagerClientException(ex.message)
        }
    }

    private fun resolveAvailableCurrenciesAddress() =
        "${currencyManagerProperties.url}$INTERNAL/currencies"

    private fun resolveExchangeRateAddress(baseCurrency: String, targetCurrency: String, date: Instant) =
        UriComponentsBuilder.fromUriString("${currencyManagerProperties.url}$INTERNAL/exchange-rate")
            .queryParam("baseCurrency", baseCurrency)
            .queryParam("targetCurrency", targetCurrency)
            .queryParam("date", date.toEpochMilli())
            .build()
            .toUriString()

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
