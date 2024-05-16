package pl.edu.agh.gem.integration.environment

import com.github.tomakehurst.wiremock.WireMockServer
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension
import org.springframework.test.context.DynamicPropertyRegistry
import java.time.Duration
import java.time.Duration.ofSeconds

object ProjectConfig : AbstractProjectConfig() {

    private const val WIREMOCK_SERVER_PORT = 9999
    const val MONGODB_IMAGE = "mongo:6.0"
    const val DATABASE_NAME = "database"
    val CONTAINER_STARTUP_TIMEOUT: Duration = ofSeconds(300L)

    val wiremock = WireMockServer(WIREMOCK_SERVER_PORT)
    private val wiremockListener = WireMockListener(wiremock)
    private val mongoListener = MongoListener()

    override fun extensions() = listOf(
        mongoListener,
        wiremockListener,
        SpringExtension,
    )

    fun updateConfiguration(registry: DynamicPropertyRegistry) {
        registry.add("spring.data.mongodb.uri") { mongoListener.url() }
        registry.add("spring.data.mongodb.database") { DATABASE_NAME }
    }
}
