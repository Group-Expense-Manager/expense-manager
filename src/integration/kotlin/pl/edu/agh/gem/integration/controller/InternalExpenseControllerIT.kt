package pl.edu.agh.gem.integration.controller

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.assertion.shouldBody
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.dto.GroupMemberResponse
import pl.edu.agh.gem.dto.GroupMembersResponse
import pl.edu.agh.gem.external.dto.expense.AcceptedGroupExpenseParticipantDto
import pl.edu.agh.gem.external.dto.expense.AcceptedGroupExpensesResponse
import pl.edu.agh.gem.external.dto.expense.GroupActivitiesResponse
import pl.edu.agh.gem.external.dto.expense.UserExpensesResponse
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.group.DummyGroup.OTHER_GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.helper.user.createGemUser
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.integration.ability.stubGroupManagerUserGroups
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.REJECTED
import pl.edu.agh.gem.internal.model.expense.filter.SortOrder.ASCENDING
import pl.edu.agh.gem.internal.model.expense.filter.SortOrder.DESCENDING
import pl.edu.agh.gem.internal.model.expense.filter.SortedBy.DATE
import pl.edu.agh.gem.internal.model.expense.filter.SortedBy.TITLE
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.util.DummyData.CURRENCY_1
import pl.edu.agh.gem.util.DummyData.CURRENCY_2
import pl.edu.agh.gem.util.DummyData.EXCHANGE_RATE_VALUE
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createExpenseParticipant
import pl.edu.agh.gem.util.createExpenseParticipants
import java.math.BigDecimal
import java.time.Instant.ofEpochMilli

class InternalExpenseControllerIT(
    private val service: ServiceTestClient,
    private val repository: ExpenseRepository,
) : BaseIntegrationSpec({

    should("get user expenses") {
        // given
        val expenseList = listOf(
            createExpense(
                id = "1",
                creatorId = USER_ID,
                totalCost = BigDecimal("60"),
                baseCurrency = CURRENCY_1,
                targetCurrency = null,
                exchangeRate = null,
                expenseParticipants = createExpenseParticipants(
                    listOf(USER_ID, "userId2", "userId3"),
                    listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
                ),
                status = ACCEPTED,
            ),

            createExpense(
                id = "2",
                creatorId = OTHER_USER_ID,
                totalCost = BigDecimal("60"),
                baseCurrency = CURRENCY_1,
                targetCurrency = CURRENCY_2,
                exchangeRate = EXCHANGE_RATE_VALUE,
                expenseParticipants = createExpenseParticipants(
                    listOf(USER_ID, OTHER_USER_ID, "userId3"),
                    listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
                ),
                status = ACCEPTED,
            ),

        )
        expenseList.forEach { repository.save(it) }

        // when
        val response = service.getUserExpenses(GROUP_ID, USER_ID)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<UserExpensesResponse> {
            userId shouldBe USER_ID
            expenses.size shouldBe 2
            expenses.first().also { userExpenses ->
                userExpenses.value shouldBe BigDecimal("50")
                userExpenses.currency shouldBe CURRENCY_1
                userExpenses.exchangeRate.shouldBeNull()
            }

            expenses.last().also { userExpenses ->
                userExpenses.value shouldBe BigDecimal("-10")
                userExpenses.currency shouldBe CURRENCY_2
                userExpenses.exchangeRate shouldBe EXCHANGE_RATE_VALUE
            }
        }
    }

    should("get accepted expenses") {
        // given
        val groupMembers = GroupMembersResponse(listOf(GroupMemberResponse(USER_ID)))
        stubGroupManagerUserGroups(groupMembers, GROUP_ID)
        val expense = createExpense(expenseParticipants = listOf(createExpenseParticipant()), status = ACCEPTED)

        repository.save(expense)

        // when
        val response = service.getAcceptedGroupExpenses(GROUP_ID)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<AcceptedGroupExpensesResponse> {
            groupId shouldBe GROUP_ID
            expenses shouldHaveSize 1
            expenses.first().also {
                it.creatorId shouldBe expense.creatorId
                it.title shouldBe expense.title
                it.totalCost shouldBe expense.totalCost
                it.baseCurrency shouldBe expense.baseCurrency
                it.targetCurrency shouldBe expense.targetCurrency
                it.exchangeRate shouldBe expense.exchangeRate?.value
                it.participants shouldBe expense.expenseParticipants
                    .map { p -> AcceptedGroupExpenseParticipantDto(p.participantId, p.participantCost) }
                it.expenseDate shouldBe expense.expenseDate
            }
        }
    }

    should("get group activities") {
        // given
        val expense = createExpense(groupId = GROUP_ID, expenseParticipants = listOf(createExpenseParticipant()))
        repository.save(expense)

        // when
        val response = service.getGroupActivitiesResponse(createGemUser(USER_ID), GROUP_ID)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<GroupActivitiesResponse> {
            groupId shouldBe GROUP_ID
            expenses shouldHaveSize 1
            expenses.first().also {
                it.expenseId shouldBe expense.id
                it.creatorId shouldBe expense.creatorId
                it.title shouldBe expense.title
                it.totalCost shouldBe expense.totalCost
                it.baseCurrency shouldBe expense.baseCurrency
                it.targetCurrency shouldBe expense.targetCurrency
                it.status shouldBe expense.status
                it.participantIds.shouldHaveSize(1)
                it.participantIds.first() shouldBe expense.expenseParticipants.first().participantId
                it.expenseDate shouldBe expense.expenseDate
            }
        }
    }

    should("get empty list when attempting to get group activities") {
        // given
        val expense = createExpense(groupId = OTHER_GROUP_ID, expenseParticipants = listOf(createExpenseParticipant()))
        repository.save(expense)

        // when
        val response = service.getGroupActivitiesResponse(createGemUser(USER_ID), GROUP_ID)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<GroupActivitiesResponse> {
            groupId shouldBe GROUP_ID
            expenses shouldHaveSize 0
        }
    }

    should("get group activities with given title") {
        // given
        val expense1 = createExpense(id = "1", groupId = GROUP_ID, title = "Pizza in Krakow")
        val expense2 = createExpense(id = "2", groupId = GROUP_ID, title = "The best burger")
        val expense3 = createExpense(id = "3", groupId = GROUP_ID, title = "Spaghetti with Andrzej")

        listOf(expense1, expense2, expense3).forEach { repository.save(it) }

        // when
        val response = service.getGroupActivitiesResponse(createGemUser(USER_ID), GROUP_ID, title = "KRA")

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<GroupActivitiesResponse> {
            groupId shouldBe GROUP_ID
            expenses shouldHaveSize 1
            expenses.first().expenseId shouldBe expense1.id
        }
    }

    should("get group activities with given status") {
        // given
        val expense1 = createExpense(id = "1", groupId = GROUP_ID, status = REJECTED)
        val expense2 = createExpense(id = "2", groupId = GROUP_ID, status = ACCEPTED)
        val expense3 = createExpense(id = "3", groupId = GROUP_ID, status = PENDING)

        listOf(expense1, expense2, expense3).forEach { repository.save(it) }

        // when
        val response = service.getGroupActivitiesResponse(createGemUser(USER_ID), GROUP_ID, status = PENDING)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<GroupActivitiesResponse> {
            groupId shouldBe GROUP_ID
            expenses shouldHaveSize 1
            expenses.first().expenseId shouldBe expense3.id
        }
    }

    should("get group activities with given creatorId") {
        // given
        val expense1 = createExpense(id = "1", groupId = GROUP_ID, creatorId = "1")
        val expense2 = createExpense(id = "2", groupId = GROUP_ID, creatorId = "2")
        val expense3 = createExpense(id = "3", groupId = GROUP_ID, creatorId = "1")

        listOf(expense1, expense2, expense3).forEach { repository.save(it) }

        // when
        val response = service.getGroupActivitiesResponse(createGemUser(USER_ID), GROUP_ID, creatorId = "1")

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<GroupActivitiesResponse> {
            groupId shouldBe GROUP_ID
            expenses shouldHaveSize 2
            expenses.map { it.expenseId } shouldContainExactly listOf(expense1.id, expense3.id)
        }
    }

    should("get group activities sorted by title") {
        // given
        val expense1 = createExpense(id = "1", groupId = GROUP_ID, title = "Pizza in Krakow")
        val expense2 = createExpense(id = "2", groupId = GROUP_ID, title = "The best burger")
        val expense3 = createExpense(id = "3", groupId = GROUP_ID, title = "Spaghetti with Andrzej")

        listOf(expense1, expense2, expense3).forEach { repository.save(it) }

        // when
        val response = service.getGroupActivitiesResponse(createGemUser(USER_ID), GROUP_ID, sortedBy = TITLE)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<GroupActivitiesResponse> {
            groupId shouldBe GROUP_ID
            expenses shouldHaveSize 3
            expenses.map { it.expenseId } shouldContainExactly listOf(expense1.id, expense3.id, expense2.id)
        }
    }

    should("get group activities sorted by expenseDate") {
        // given
        val expense1 = createExpense(id = "1", groupId = GROUP_ID, expenseDate = ofEpochMilli(2))
        val expense2 = createExpense(id = "2", groupId = GROUP_ID, expenseDate = ofEpochMilli(3))
        val expense3 = createExpense(id = "3", groupId = GROUP_ID, expenseDate = ofEpochMilli(1))

        listOf(expense1, expense2, expense3).forEach { repository.save(it) }

        // when
        val response = service.getGroupActivitiesResponse(createGemUser(USER_ID), GROUP_ID, sortedBy = DATE)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<GroupActivitiesResponse> {
            groupId shouldBe GROUP_ID
            expenses shouldHaveSize 3
            expenses.map { it.expenseId } shouldContainExactly listOf(expense3.id, expense1.id, expense2.id)
        }
    }

    should("get group activities sorted by expenseDate ascending") {
        // given
        val expense1 = createExpense(id = "1", groupId = GROUP_ID, expenseDate = ofEpochMilli(2))
        val expense2 = createExpense(id = "2", groupId = GROUP_ID, expenseDate = ofEpochMilli(3))
        val expense3 = createExpense(id = "3", groupId = GROUP_ID, expenseDate = ofEpochMilli(1))

        listOf(expense1, expense2, expense3).forEach { repository.save(it) }

        // when
        val response = service.getGroupActivitiesResponse(createGemUser(USER_ID), GROUP_ID, sortOrder = ASCENDING)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<GroupActivitiesResponse> {
            groupId shouldBe GROUP_ID
            expenses shouldHaveSize 3
            expenses.map { it.expenseId } shouldContainExactly listOf(expense3.id, expense1.id, expense2.id)
        }
    }

    should("get group activities sorted by expenseDate descending") {
        // given
        val expense1 = createExpense(id = "1", groupId = GROUP_ID, expenseDate = ofEpochMilli(2))
        val expense2 = createExpense(id = "2", groupId = GROUP_ID, expenseDate = ofEpochMilli(3))
        val expense3 = createExpense(id = "3", groupId = GROUP_ID, expenseDate = ofEpochMilli(1))

        listOf(expense1, expense2, expense3).forEach { repository.save(it) }

        // when
        val response = service.getGroupActivitiesResponse(createGemUser(USER_ID), GROUP_ID, sortOrder = DESCENDING)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<GroupActivitiesResponse> {
            groupId shouldBe GROUP_ID
            expenses shouldHaveSize 3
            expenses.map { it.expenseId } shouldContainExactly listOf(expense2.id, expense1.id, expense3.id)
        }
    }
},)
