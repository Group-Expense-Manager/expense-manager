package pl.edu.agh.gem.external.client

import io.github.resilience4j.retry.annotation.Retry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.POST
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.config.FinanceAdapterProperties
import pl.edu.agh.gem.external.dto.group.GroupResponse
import pl.edu.agh.gem.external.dto.group.toDto
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.internal.client.FinanceAdapterClient
import pl.edu.agh.gem.internal.client.FinanceAdapterClientException
import pl.edu.agh.gem.internal.client.GroupManagerClientException
import pl.edu.agh.gem.internal.client.RetryableFinanceAdapterClientException
import pl.edu.agh.gem.internal.model.currency.Currency
import pl.edu.agh.gem.paths.Paths.INTERNAL

@Component
class RestFinanceAdapterClient(
    @Qualifier("FinanceAdapterRestTemplate") val restTemplate: RestTemplate,
    private val financeAdapterProperties: FinanceAdapterProperties,
) : FinanceAdapterClient {

    @Retry(name = "financeAdapterClient")
    override fun generate(groupId: String, currency: Currency) {
        try {
            restTemplate.exchange(
                resolveGenerateAddress(groupId),
                POST,
                HttpEntity(currency.toDto(), HttpHeaders().withAppContentType()),
                GroupResponse::class.java,
            )
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to generate balance & settlement for group: $groupId and currency: $currency" }
            throw GroupManagerClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to generate balance & settlement for group: $groupId and currency: $currency" }
            throw RetryableFinanceAdapterClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to generate balance & settlement for group: $groupId and currency: $currency" }
            throw FinanceAdapterClientException(ex.message)
        }
    }

    private fun resolveGenerateAddress(groupId: String) =
        "${financeAdapterProperties.url}$INTERNAL/generate/groups/$groupId"

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
