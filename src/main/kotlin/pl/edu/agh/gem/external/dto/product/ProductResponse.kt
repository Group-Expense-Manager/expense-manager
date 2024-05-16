package pl.edu.agh.gem.external.dto.product

import pl.edu.agh.gem.internal.model.Product
import pl.edu.agh.gem.security.GemUser

data class ProductResponse(
    val id: String,
    val name: String,
    val forUser: String,
) {
    companion object {
        fun from(product: Product, user: GemUser): ProductResponse {
            return ProductResponse(
                id = product.id,
                name = product.name,
                forUser = user.email,
            )
        }
    }
}
