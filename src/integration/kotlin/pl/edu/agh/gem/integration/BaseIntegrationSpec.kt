package pl.edu.agh.gem.integration

import io.kotest.core.spec.style.ShouldSpec
import mu.KotlinLogging
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import pl.edu.agh.gem.AppRunner
import pl.edu.agh.gem.integration.environment.ProjectConfig

@SpringBootTest(
    classes = [AppRunner::class],
    webEnvironment = RANDOM_PORT,
)
@ActiveProfiles("integration")
abstract class BaseIntegrationSpec(body: ShouldSpec.() -> Unit) : ShouldSpec(body) {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun injectContainerData(registry: DynamicPropertyRegistry) {
            logger.info { "Injecting configuration" }
            ProjectConfig.updateConfiguration(registry)
        }

        private val logger = KotlinLogging.logger {}
    }
}
