package pl.edu.agh.gem.integration.ability

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatusCode
import pl.edu.agh.gem.headers.HeadersTestUtils.withAppAcceptType
import pl.edu.agh.gem.headers.HeadersTestUtils.withAppContentType
import pl.edu.agh.gem.integration.environment.ProjectConfig.wiremock

private const val EXAMPLE_URL = "/api/example"

private fun createUrl(id: String) =
    "$EXAMPLE_URL/$id"

private fun createUrlForAnyId() =
    "$EXAMPLE_URL/[^/]+"

fun stubExamplePostProduct(statusCode: HttpStatusCode = OK) {
    wiremock.stubFor(
        post(urlMatching(EXAMPLE_URL))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppAcceptType(),
            ),
    )
}

fun stubExampleGetProduct(body: Any?, id: String, statusCode: HttpStatusCode = OK) {
    wiremock.stubFor(
        get(urlMatching(createUrl(id)))
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
