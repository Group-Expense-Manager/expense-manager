package pl.edu.agh.gem.integration.environment

import com.github.tomakehurst.wiremock.WireMockServer
import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.listeners.BeforeProjectListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import mu.KotlinLogging

class WireMockListener(private val wireMockServer: WireMockServer) :
    AfterTestListener,
    BeforeProjectListener,
    AfterProjectListener {
    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
        wireMockServer.resetAll()
    }

    override suspend fun beforeProject() {
        logger.info { "Starting Wiremock" }
        wireMockServer.start()
    }

    override suspend fun afterProject() {
        wireMockServer.stop()
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
