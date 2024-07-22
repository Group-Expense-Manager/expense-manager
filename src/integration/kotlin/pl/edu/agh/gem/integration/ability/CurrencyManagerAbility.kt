package pl.edu.agh.gem.integration.ability

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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

private fun createGroupDataUrl() =
    "$INTERNAL/currencies"

private fun createExchangeRateUrl(baseCurrency: String, targetCurrency: String, date: Instant) =
    UriComponentsBuilder.fromUriString("$INTERNAL/currencies/from/$baseCurrency/to/$targetCurrency/")
        .queryParam("date", date.toString())
        .build()
        .toUriString()

fun stubCurrencyManagerAvailableCurrencies(body: Any?, statusCode: HttpStatusCode = OK) {
    wiremock.stubFor(
        get(urlMatching(createGroupDataUrl()))
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
                        jacksonObjectMapper().registerModules(JavaTimeModule()).writeValueAsString(body),
                    ),
            ),
    )
}
