package pl.edu.agh.gem.integration.ability

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatusCode
import pl.edu.agh.gem.headers.HeadersTestUtils.withAppContentType
import pl.edu.agh.gem.integration.environment.ProjectConfig.wiremock
import pl.edu.agh.gem.paths.Paths.INTERNAL

private fun createMembersUrl(groupId: String) =
    "$INTERNAL/members/$groupId"

private fun createOptionsUrl(groupId: String) =
    "$INTERNAL/options/$groupId"

fun stubGroupManagerMembers(body: Any?, groupId: String, statusCode: HttpStatusCode = OK) {
    wiremock.stubFor(
        get(urlMatching(createMembersUrl(groupId)))
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

fun stubGroupManagerOptions(body: Any?, groupId: String, statusCode: HttpStatusCode = OK) {
    wiremock.stubFor(
        get(urlMatching(createOptionsUrl(groupId)))
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
