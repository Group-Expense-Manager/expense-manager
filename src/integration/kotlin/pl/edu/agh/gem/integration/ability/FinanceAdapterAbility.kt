package pl.edu.agh.gem.integration.ability

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatusCode
import pl.edu.agh.gem.external.dto.group.CurrencyDTO
import pl.edu.agh.gem.integration.environment.ProjectConfig.wiremock
import pl.edu.agh.gem.paths.Paths.INTERNAL

private fun createGenerateUrl(groupId: String) = "$INTERNAL/generate/groups/$groupId"

fun stubFinanceAdapterGenerate(requestBody: CurrencyDTO, groupId: String, statusCode: HttpStatusCode = OK) {
    wiremock.stubFor(
        post(urlMatching(createGenerateUrl(groupId)))
            .withRequestBody(
                equalToJson(
                    jacksonObjectMapper().writeValueAsString(requestBody),
                ),
            ).willReturn(
                aResponse()
                    .withStatus(statusCode.value()),

            ),
    )
}
