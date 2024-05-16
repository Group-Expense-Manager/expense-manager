package pl.edu.agh.gem.util

import pl.edu.agh.gem.external.dto.example.ExampleProductResponse
import pl.edu.agh.gem.external.dto.product.ProductRequest
import pl.edu.agh.gem.internal.model.Product
import pl.edu.agh.gem.security.GemUser

fun createProduct(
    id: String = "id",
    name: String = "name",
) = Product(
    id = id,
    name = name,
)

fun createProductRequest(
    name: String = "name",
) = ProductRequest(
    name = name,
)

fun createExampleProductResponse(
    id: String = "id",
    name: String = "name",
) = ExampleProductResponse(
    id = id,
    name = name,
)

fun createGemUser(
    id: String = "id",
    email: String = "email",
) = GemUser(
    id = id,
    email = email,
)
