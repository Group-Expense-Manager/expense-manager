package pl.edu.agh.gem.integration.client

import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubExampleGetProduct
import pl.edu.agh.gem.internal.client.ExampleClient
import pl.edu.agh.gem.util.createExampleProductResponse

class RestExampleClientIT(
    private val exampleClient: ExampleClient,
) : BaseIntegrationSpec({
    should("get product") {
        // given
        val exampleProductResponse = createExampleProductResponse()
        stubExampleGetProduct(exampleProductResponse, exampleProductResponse.id, OK)

        // when
        val result = exampleClient.getProduct(exampleProductResponse.id)

        // then
        result.also {
            it.id shouldBe exampleProductResponse.id
            it.name shouldBe exampleProductResponse.name
        }
    }
},)
