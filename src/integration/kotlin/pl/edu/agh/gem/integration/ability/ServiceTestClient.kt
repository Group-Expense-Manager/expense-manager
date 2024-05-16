package pl.edu.agh.gem.integration.ability

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.servlet.client.MockMvcWebTestClient.bindToApplicationContext
import org.springframework.web.context.WebApplicationContext
import pl.edu.agh.gem.external.dto.product.ProductRequest
import pl.edu.agh.gem.headers.HeadersUtils.withAppAcceptType
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.headers.HeadersUtils.withValidatedUser
import pl.edu.agh.gem.security.GemUser
import java.net.URI

@Component
@Lazy
class ServiceTestClient(applicationContext: WebApplicationContext) {
    private val webClient = bindToApplicationContext(applicationContext)
        .configureClient()
        .build()

    fun findProduct(id: String, user: GemUser): ResponseSpec {
        return webClient.get()
            .uri(URI("/api/products/$id"))
            .headers { it.withAppAcceptType().withValidatedUser(user) }
            .exchange()
    }

    fun createProduct(body: ProductRequest): ResponseSpec {
        return webClient.post()
            .uri(URI("/api/products"))
            .headers { it.withAppContentType() }
            .bodyValue(body)
            .exchange()
    }
}
