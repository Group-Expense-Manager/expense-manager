package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.Product

interface ExampleClient {
    fun postProduct(product: Product)

    fun getProduct(productId: String): Product
}

class ExampleClientException(override val message: String?) : RuntimeException()

class RetryableExampleClientException(override val message: String?) : RuntimeException()
