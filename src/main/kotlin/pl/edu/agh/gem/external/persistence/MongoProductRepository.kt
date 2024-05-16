package pl.edu.agh.gem.external.persistence

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.where
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.Product
import pl.edu.agh.gem.internal.persistence.ProductRepository

@Repository
class MongoProductRepository(private val mongo: MongoTemplate) : ProductRepository {

    override fun find(id: String): Product? {
        val query = Query.query(where(ProductEntity::id).isEqualTo(id))
        return mongo.findOne(query, ProductEntity::class.java)?.toDomain()
    }

    override fun save(product: Product) {
        mongo.save(product.toEntity())
    }
}

private fun Product.toEntity() = ProductEntity(
    id = id,
    name = name,
)

private fun ProductEntity.toDomain() = Product(
    id = id,
    name = name,
)
