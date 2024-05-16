package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.Product

interface ProductRepository {
    fun find(id: String): Product?

    fun save(product: Product)
}
