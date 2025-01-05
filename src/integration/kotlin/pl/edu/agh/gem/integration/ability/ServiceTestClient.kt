package pl.edu.agh.gem.integration.ability

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.servlet.client.MockMvcWebTestClient.bindToApplicationContext
import org.springframework.web.context.WebApplicationContext
import pl.edu.agh.gem.headers.HeadersUtils.withAppAcceptType
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.headers.HeadersUtils.withValidatedUser
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.filter.SortOrder
import pl.edu.agh.gem.internal.model.expense.filter.SortOrder.ASCENDING
import pl.edu.agh.gem.internal.model.expense.filter.SortedBy
import pl.edu.agh.gem.internal.model.expense.filter.SortedBy.DATE
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import pl.edu.agh.gem.paths.Paths.INTERNAL
import pl.edu.agh.gem.security.GemUser
import java.net.URI
import java.util.Optional

@Component
@Lazy
class ServiceTestClient(applicationContext: WebApplicationContext) {
    private val webClient =
        bindToApplicationContext(applicationContext)
            .configureClient()
            .build()

    fun createExpense(
        body: Any,
        user: GemUser,
        groupId: String,
    ): ResponseSpec {
        return webClient.post()
            .uri { it.path("$EXTERNAL/expenses").queryParam("groupId", groupId).build() }
            .headers {
                it.withValidatedUser(user)
                it.withAppContentType()
            }
            .bodyValue(body)
            .exchange()
    }

    fun getExpense(
        user: GemUser,
        expenseId: String,
        groupId: String,
    ): ResponseSpec {
        return webClient.get()
            .uri(URI("$EXTERNAL/expenses/$expenseId/groups/$groupId"))
            .headers { it.withValidatedUser(user).withAppAcceptType() }
            .exchange()
    }

    fun getGroupActivitiesResponse(
        user: GemUser,
        groupId: String,
        title: String? = null,
        status: ExpenseStatus? = null,
        creatorId: String? = null,
        sortedBy: SortedBy = DATE,
        sortOrder: SortOrder = ASCENDING,
    ): ResponseSpec {
        return webClient.get()
            .uri {
                it.path("$INTERNAL/expenses/activities/groups/$groupId")
                    .queryParamIfPresent("title", Optional.ofNullable(title))
                    .queryParamIfPresent("status", Optional.ofNullable(status))
                    .queryParamIfPresent("creatorId", Optional.ofNullable(creatorId))
                    .queryParam("sortedBy", sortedBy)
                    .queryParam("sortOrder", sortOrder).build()
            }
            .headers { it.withValidatedUser(user).withAppAcceptType() }
            .exchange()
    }

    fun decide(
        body: Any,
        user: GemUser,
    ): ResponseSpec {
        return webClient.post()
            .uri(URI("$EXTERNAL/expenses/decide"))
            .headers { it.withValidatedUser(user).withAppContentType() }
            .bodyValue(body)
            .exchange()
    }

    fun delete(
        user: GemUser,
        groupId: String,
        expenseId: String,
    ): ResponseSpec {
        return webClient.delete()
            .uri(URI("$EXTERNAL/expenses/$expenseId/groups/$groupId"))
            .headers { it.withValidatedUser(user) }
            .exchange()
    }

    fun getUserExpenses(
        groupId: String,
        userId: String,
    ): ResponseSpec {
        return webClient.get()
            .uri(URI("$INTERNAL/expenses/groups/$groupId/users/$userId"))
            .headers { it.withAppAcceptType() }
            .exchange()
    }

    fun getAcceptedGroupExpenses(
        groupId: String,
        currency: String,
    ): ResponseSpec {
        return webClient.get()
            .uri { it.path("$INTERNAL/expenses/accepted/groups/$groupId").queryParam("currency", currency).build() }
            .headers { it.withAppAcceptType() }
            .exchange()
    }

    fun updateExpense(
        body: Any,
        user: GemUser,
        groupId: String,
        expenseId: String,
    ): ResponseSpec {
        return webClient.put()
            .uri(URI("$EXTERNAL/expenses/$expenseId/groups/$groupId"))
            .headers {
                it.withValidatedUser(user)
                it.withAppContentType()
            }
            .bodyValue(body)
            .exchange()
    }
}
