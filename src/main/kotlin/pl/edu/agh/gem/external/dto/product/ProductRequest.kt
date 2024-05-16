package pl.edu.agh.gem.external.dto.product

import jakarta.validation.constraints.NotBlank
import pl.edu.agh.gem.internal.model.Product
import java.util.UUID

data class ProductRequest(
    @field:NotBlank val name: String,
) {
    fun toDomain() = Product(
        id = UUID.randomUUID().toString(),
        name = name,
    )
}
