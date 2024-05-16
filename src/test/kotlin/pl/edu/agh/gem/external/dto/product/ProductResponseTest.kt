package pl.edu.agh.gem.external.dto.product

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.util.createGemUser
import pl.edu.agh.gem.util.createProduct

class ProductResponseTest : ShouldSpec({

    should("map correct to Product") {
        // given
        val product = createProduct(
            id = "id",
            name = "name",
        )
        val user = createGemUser(
            email = "email",
        )

        // when
        val productResponse = ProductResponse.from(product, user)

        // then
        productResponse.also {
            it.id shouldBe "id"
            it.name shouldBe "name"
            it.forUser shouldBe "email"
        }
    }
},)
