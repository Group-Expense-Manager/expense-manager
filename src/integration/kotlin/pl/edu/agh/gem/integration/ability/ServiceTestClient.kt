package pl.edu.agh.gem.integration.ability

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.servlet.client.MockMvcWebTestClient.bindToApplicationContext
import org.springframework.web.context.WebApplicationContext
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.headers.HeadersUtils.withValidatedUser
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import pl.edu.agh.gem.security.GemUser

@Component
@Lazy
class ServiceTestClient(applicationContext: WebApplicationContext) {
    private val webClient = bindToApplicationContext(applicationContext)
        .configureClient()
        .build()

    fun createExpense(body: Any, user: GemUser, groupId: String): ResponseSpec {
        return webClient.post()
            .uri { it.path("$EXTERNAL/expenses").queryParam("groupId", groupId).build() }
            .headers {
                it.withValidatedUser(user)
                it.withAppContentType()
            }
            .bodyValue(body)
            .exchange()
    }
}
