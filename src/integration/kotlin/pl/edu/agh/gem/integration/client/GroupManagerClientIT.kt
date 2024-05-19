package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.group.createGroupMembers
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubGroupManagerMembers
import pl.edu.agh.gem.integration.ability.stubGroupManagerOptions
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.client.GroupManagerClientException
import pl.edu.agh.gem.internal.client.RetryableGroupManagerClientException
import pl.edu.agh.gem.internal.model.currency.Currencies
import pl.edu.agh.gem.internal.model.currency.Currency
import pl.edu.agh.gem.util.createGroupOptionsResponse

class GroupManagerClientIT(
    private val groupManagerClient: GroupManagerClient,
) : BaseIntegrationSpec({
    should("get group Options") {
        // given
        val listOfCurrencies = listOf("PLN", "USD", "EUR")
        val groupOptions = createGroupOptionsResponse(acceptRequired = true, currencies = Currencies(listOfCurrencies.map { Currency(it) }))
        stubGroupManagerOptions(groupOptions, GROUP_ID)

        // when
        val result = groupManagerClient.getGroupOptions(GROUP_ID)

        // then
        result.also {
            it shouldNotBe null
            it.acceptRequired shouldBe true
            it.currencies.currencies.map { currency -> currency.code } shouldBe listOfCurrencies
        }
    }

    should("throw GroupManagerClientException when we send bad request") {
        // given
        val groupOptions = createGroupOptionsResponse()
        stubGroupManagerOptions(groupOptions, GROUP_ID, NOT_ACCEPTABLE)

        // when & then
        shouldThrow<GroupManagerClientException> {
            groupManagerClient.getGroupOptions(GROUP_ID)
        }
    }

    should("throw RetryableCurrencyManagerClientException when client has internal error") {
        // given
        val groupOptions = createGroupOptionsResponse()
        stubGroupManagerOptions(groupOptions, GROUP_ID, INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableGroupManagerClientException> {
            groupManagerClient.getGroupOptions(GROUP_ID)
        }
    }

    should("get members") {
        // given
        val listOfMembers = listOf(USER_ID, OTHER_USER_ID)
        val groupMembers = createGroupMembers(listOfMembers)
        stubGroupManagerMembers(groupMembers, GROUP_ID)

        // when
        val result = groupManagerClient.getGroupMembers(GROUP_ID)

        // then
        result.members.all {
            it.id in listOfMembers
        }
    }

    should("throw GroupManagerClientException when we send bad request") {
        // given
        stubGroupManagerMembers(createGroupMembers(listOf()), GROUP_ID, NOT_ACCEPTABLE)

        // when & then
        shouldThrow<GroupManagerClientException> {
            groupManagerClient.getGroupMembers(GROUP_ID)
        }
    }

    should("throw RetryableGroupManagerClientException when client has internal error") {
        // given
        stubGroupManagerMembers(createGroupMembers(listOf()), GROUP_ID, INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableGroupManagerClientException> {
            groupManagerClient.getGroupMembers(GROUP_ID)
        }
    }
},)
