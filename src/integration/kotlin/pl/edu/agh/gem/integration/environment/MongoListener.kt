package pl.edu.agh.gem.integration.environment

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.listeners.BeforeProjectListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import mu.KotlinLogging
import org.bson.Document
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.wait.strategy.Wait
import pl.edu.agh.gem.integration.environment.ProjectConfig.CONTAINER_STARTUP_TIMEOUT
import pl.edu.agh.gem.integration.environment.ProjectConfig.DATABASE_NAME
import pl.edu.agh.gem.integration.environment.ProjectConfig.MONGODB_IMAGE

class MongoListener : BeforeProjectListener, AfterProjectListener, AfterTestListener {
    private lateinit var container: MongoDBContainer
    private lateinit var client: MongoClient

    fun url(): String = container.replicaSetUrl

    override suspend fun beforeProject() {
        logger.info { "Preparing to start image '$MONGODB_IMAGE'" }
        container = MongoDBContainer(MONGODB_IMAGE)
        container.start()
        container.waitingFor(
            Wait.forListeningPort().withStartupTimeout(CONTAINER_STARTUP_TIMEOUT),
        )
        logger.info("MongoDB container started at port(s) ${container.replicaSetUrl}")
        client = MongoClients.create(container.replicaSetUrl)
    }

    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
        with(client.getDatabase(DATABASE_NAME)) {
            listCollectionNames().forEach {
                getCollection(it).deleteMany(Document())
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
