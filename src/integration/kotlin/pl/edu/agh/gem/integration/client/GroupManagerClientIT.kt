package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.group.DummyGroup.OTHER_GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubGroupManagerGroupData
import pl.edu.agh.gem.integration.ability.stubGroupManagerUserGroups
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.client.GroupManagerClientException
import pl.edu.agh.gem.internal.client.RetryableGroupManagerClientException
import pl.edu.agh.gem.internal.model.currency.Currency
import pl.edu.agh.gem.model.GroupMember
import pl.edu.agh.gem.util.createCurrenciesDTO
import pl.edu.agh.gem.util.createGroupResponse
import pl.edu.agh.gem.util.createMembersDTO
import pl.edu.agh.gem.util.createUserGroupsResponse

class GroupManagerClientIT(
    private val groupManagerClient: GroupManagerClient,
) : BaseIntegrationSpec({
        should("get group") {
            // given
            val members = createMembersDTO(USER_ID, OTHER_USER_ID)
            val listOfCurrencies = createCurrenciesDTO("PLN", "USD", "EUR")
            val groupResponse = createGroupResponse(members = members, groupCurrencies = listOfCurrencies)
            stubGroupManagerGroupData(groupResponse, GROUP_ID)

            // when
            val result = groupManagerClient.getGroup(GROUP_ID)

            // then
            result.also {
                it.shouldNotBeNull()
                it.currencies shouldBe listOfCurrencies.map { currency -> Currency(currency.code) }
                it.members.members shouldBe members.map { member -> GroupMember(member.id) }
            }
        }

        should("throw GroupManagerClientException when we send bad request") {
            // given
            val groupResponse = createGroupResponse()
            stubGroupManagerGroupData(groupResponse, GROUP_ID, NOT_ACCEPTABLE)

            // when & then
            shouldThrow<GroupManagerClientException> {
                groupManagerClient.getGroup(GROUP_ID)
            }
        }

        should("throw RetryableCurrencyManagerClientException when client has internal error") {
            // given
            val groupResponse = createGroupResponse()
            stubGroupManagerGroupData(groupResponse, GROUP_ID, INTERNAL_SERVER_ERROR)

            // when & then
            shouldThrow<RetryableGroupManagerClientException> {
                groupManagerClient.getGroup(GROUP_ID)
            }
        }

        should("get user groups") {
            // given
            val userGroups = arrayOf(GROUP_ID, OTHER_GROUP_ID)
            val userGroupsResponse = createUserGroupsResponse(GROUP_ID, OTHER_GROUP_ID)
            stubGroupManagerUserGroups(userGroupsResponse, USER_ID)

            // when
            val result = groupManagerClient.getUserGroups(USER_ID)

            // then
            result.all {
                it.groupId in userGroups
            }
        }

        should("throw GroupManagerClientException when we send bad request") {
            // given
            stubGroupManagerUserGroups(createUserGroupsResponse(), USER_ID, NOT_ACCEPTABLE)

            // when & then
            shouldThrow<GroupManagerClientException> {
                groupManagerClient.getUserGroups(USER_ID)
            }
        }

        should("throw RetryableGroupManagerClientException when client has internal error") {
            // given
            stubGroupManagerUserGroups(createUserGroupsResponse(), USER_ID, INTERNAL_SERVER_ERROR)

            // when & then
            shouldThrow<RetryableGroupManagerClientException> {
                groupManagerClient.getUserGroups(USER_ID)
            }
        }
    })
