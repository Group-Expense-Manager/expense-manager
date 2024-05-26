package pl.edu.agh.gem.external.client

import io.github.resilience4j.retry.annotation.Retry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.config.GroupManagerProperties
import pl.edu.agh.gem.dto.GroupMembersResponse
import pl.edu.agh.gem.dto.toDomain
import pl.edu.agh.gem.external.dto.group.GroupResponse
import pl.edu.agh.gem.headers.HeadersUtils.withAppAcceptType
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.client.GroupManagerClientException
import pl.edu.agh.gem.internal.client.RetryableGroupManagerClientException
import pl.edu.agh.gem.internal.model.group.Group
import pl.edu.agh.gem.model.GroupMembers
import pl.edu.agh.gem.paths.Paths.INTERNAL

@Component
class RestGroupManagerClient(
    @Qualifier("GroupManagerRestTemplate") val restTemplate: RestTemplate,
    val groupManagerProperties: GroupManagerProperties,
) : GroupManagerClient {

    @Retry(name = "groupManagerClient")
    override fun getMembers(groupId: String): GroupMembers {
        return try {
            restTemplate.exchange(
                resolveMembersAddress(groupId),
                GET,
                HttpEntity<Any>(HttpHeaders().withAppAcceptType().withAppContentType()),
                GroupMembersResponse::class.java,
            ).body?.toDomain() ?: throw GroupManagerClientException(
                "While retrieving members of group using GroupManagerClient we receive empty body",
            )
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to get members of group: $groupId" }
            throw GroupManagerClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to get members of group: $groupId" }
            throw RetryableGroupManagerClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to get members of group: $groupId" }
            throw GroupManagerClientException(ex.message)
        }
    }

    @Retry(name = "groupManagerClient")
    override fun getGroup(groupId: String): Group {
        return try {
            restTemplate.exchange(
                resolveGroupAddress(groupId),
                GET,
                HttpEntity<Any>(HttpHeaders().withAppAcceptType().withAppContentType()),
                GroupResponse::class.java,
            ).body?.toDomain() ?: throw GroupManagerClientException(
                "While retrieving group using GroupManagerClient we receive empty body",
            )
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to get group: $groupId" }
            throw GroupManagerClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to get group: $groupId" }
            throw RetryableGroupManagerClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to get group: $groupId" }
            throw GroupManagerClientException(ex.message)
        }
    }

    private fun resolveMembersAddress(groupId: String) =
        "${groupManagerProperties.url}$INTERNAL/members/$groupId"

    private fun resolveGroupAddress(groupId: String) =
        "${groupManagerProperties.url}$INTERNAL/groups/$groupId"

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
