package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.client.ExampleClient
import pl.edu.agh.gem.internal.model.Product
import pl.edu.agh.gem.internal.persistence.ProductRepository

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val exampleClient: ExampleClient,
) {
    fun find(id: String): Product {
        return productRepository.find(id) ?: throw MissingProductException(id)
    }

    fun save(product: Product) {
        return productRepository.save(product)
    }

    fun send(product: Product) {
        exampleClient.postProduct(product)
    }
}

class MissingProductException(id: String) : RuntimeException("Failed to find product with id:$id")
