package pl.edu.agh.gem.external.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.external.dto.product.ProductRequest
import pl.edu.agh.gem.external.dto.product.ProductResponse
import pl.edu.agh.gem.internal.service.ProductService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.security.GemAuthenticatedUser
import pl.edu.agh.gem.security.GemUser

@RestController
@RequestMapping("/api/products")
class ProductController(
    val productService: ProductService,
) {

    @GetMapping("/{id}", produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun findOne(
        @PathVariable id: String,
        @GemAuthenticatedUser user: GemUser,
    ): ProductResponse {
        val product = productService.find(id)
        return ProductResponse.from(product, user)
    }

    @PostMapping(consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun createProduct(
        @Valid @RequestBody
        productRequest: ProductRequest,
    ) {
        productService.save(productRequest.toDomain())
    }
}
