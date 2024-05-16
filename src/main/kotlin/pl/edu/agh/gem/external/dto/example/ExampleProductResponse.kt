package pl.edu.agh.gem.external.dto.example

import pl.edu.agh.gem.internal.model.Product

data class ExampleProductResponse(
    val id: String,
    val name: String,
) {
    fun toDomain() =
        Product(
            id = id,
            name = name,
        )
}
