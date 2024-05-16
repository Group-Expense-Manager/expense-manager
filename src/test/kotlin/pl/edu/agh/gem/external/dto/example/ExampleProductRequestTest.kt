package pl.edu.agh.gem.external.dto.example

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.util.createProduct

class ExampleProductRequestTest : ShouldSpec({

    should("map correct to Product") {
        // given
        val product = createProduct(
            id = "id",
            name = "name",
        )
        // when
        val exampleProductRequest = ExampleProductRequest.from(product)

        // then
        exampleProductRequest.also {
            it.id shouldBe "id"
            it.name shouldBe "name"
        }
    }
},)
