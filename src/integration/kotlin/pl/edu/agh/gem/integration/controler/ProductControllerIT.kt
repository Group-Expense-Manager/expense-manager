package pl.edu.agh.gem.integration.controler

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.assertion.shouldHaveBody
import pl.edu.agh.gem.assertion.shouldHaveErrors
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.external.dto.product.ProductResponse
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.internal.persistence.ProductRepository
import pl.edu.agh.gem.util.createGemUser
import pl.edu.agh.gem.util.createProduct
import pl.edu.agh.gem.util.createProductRequest

class ProductControllerIT(
    private val service: ServiceTestClient,
    private val productRepository: ProductRepository,
) : BaseIntegrationSpec({
    should("find product") {
        // given
        val product = createProduct()
        val user = createGemUser()
        productRepository.save(product)

        // when
        val response = service.findProduct(product.id, user)

        // then
        response shouldHaveHttpStatus OK
        response shouldHaveBody ProductResponse(
            id = product.id,
            name = product.name,
            forUser = user.email,
        )
    }

    should("create product") {
        // given
        val productRequest = createProductRequest()

        // when
        val response = service.createProduct(productRequest)

        // then
        response shouldHaveHttpStatus OK
    }

    should("throw error when product with given id does not exist") {
        // given
        val productRequest = createProductRequest(name = "")

        // when
        val response = service.createProduct(productRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveErrors {
            errors.size shouldBe 1
            errors[0].code shouldBe "VALIDATION_ERROR"
            errors[0].message shouldBe "must not be blank"
            errors[0].details shouldBe "name"
            errors[0].path.shouldBeNull()
            errors[0].userMessage shouldBe "must not be blank"
        }
    }
},)
