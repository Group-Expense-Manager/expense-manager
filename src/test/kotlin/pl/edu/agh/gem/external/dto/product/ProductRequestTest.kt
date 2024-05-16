package pl.edu.agh.gem.external.dto.product

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.util.createProductRequest

class ProductRequestTest : ShouldSpec({

    should("map correct to Product") {
        // given
        val productRequest = createProductRequest(
            name = "name",
        )
        // when
        val product = productRequest.toDomain()

        // then
        product.also {
            it.id.shouldNotBeNull()
            it.name shouldBe "name"
        }
    }
},)
