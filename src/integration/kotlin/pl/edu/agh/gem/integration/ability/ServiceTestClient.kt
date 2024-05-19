package pl.edu.agh.gem.integration.ability

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.client.MockMvcWebTestClient.bindToApplicationContext
import org.springframework.web.context.WebApplicationContext

@Component
@Lazy
class ServiceTestClient(applicationContext: WebApplicationContext) {
    private val webClient = bindToApplicationContext(applicationContext)
        .configureClient()
        .build()
}
