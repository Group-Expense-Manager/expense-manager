package pl.edu.agh.gem.integration.ability

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatusCode
import org.springframework.web.util.UriComponentsBuilder
import pl.edu.agh.gem.headers.HeadersTestUtils.withAppContentType
import pl.edu.agh.gem.integration.environment.ProjectConfig.wiremock
import pl.edu.agh.gem.paths.Paths.INTERNAL
import java.time.Instant
import java.time.format.DateTimeFormatter.ISO_INSTANT

private fun createGroupsUrl() =
    "$INTERNAL/currencies"

private fun createExchangeRateUrl(baseCurrency: String, targetCurrency: String, date: Instant) =
    UriComponentsBuilder.fromUriString("$INTERNAL/exchange-rate")
        .queryParam("baseCurrency", baseCurrency)
        .queryParam("targetCurrency", targetCurrency)
        .queryParam("date", ISO_INSTANT.format(date))
        .build()
        .toUriString()

fun stubCurrencyManagerAvailableCurrencies(body: Any?, statusCode: HttpStatusCode = OK) {
    wiremock.stubFor(
        get(urlMatching(createGroupsUrl()))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType()
                    .withBody(
                        jacksonObjectMapper().writeValueAsString(body),
                    ),
            ),
    )
}

fun stubCurrencyManagerExchangeRate(body: Any?, baseCurrency: String, targetCurrency: String, date: Instant, statusCode: HttpStatusCode = OK) {
    wiremock.stubFor(
        get(createExchangeRateUrl(baseCurrency, targetCurrency, date))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType()
                    .withBody(
                        jacksonObjectMapper().writeValueAsString(body),
                    ),
            ),
    )
}
