package pl.edu.agh.gem.external.dto.example

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.util.createExampleProductResponse

class ExampleProductResponseTest : ShouldSpec({

    should("map correct to Product") {
        // given
        val exampleProductResponse = createExampleProductResponse(
            id = "id",
            name = "name",
        )
        // when
        val product = exampleProductResponse.toDomain()

        // then
        product.also {
            it.id shouldBe "id"
            it.name shouldBe "name"
        }
    }
},)
