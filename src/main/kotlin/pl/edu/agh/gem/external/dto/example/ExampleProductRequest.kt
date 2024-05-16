package pl.edu.agh.gem.external.dto.example

import pl.edu.agh.gem.internal.model.Product

data class ExampleProductRequest(
    val id: String,
    val name: String,
) {
    companion object {
        fun from(product: Product) =
            ExampleProductResponse(
                id = product.id,
                name = product.name,
            )
    }
}
