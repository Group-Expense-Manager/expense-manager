package pl.edu.agh.gem.external.dto.expense

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createExpenseParticipant
import pl.edu.agh.gem.util.createListOfAcceptedGroupExpenseParticipantDto
import java.math.BigDecimal
import java.time.Instant

class AcceptedGroupExpensesResponseTest : ShouldSpec({

    should("map Expense to AcceptedGroupExpensesResponse") {
        // given
        val expense = createExpense(expenseParticipants = listOf(createExpenseParticipant()))

        // when
        val groupExpensesResponse = listOf(expense).toAcceptedGroupExpensesResponse(GROUP_ID)

        // then
        groupExpensesResponse.groupId shouldBe GROUP_ID
        groupExpensesResponse.expenses shouldHaveSize 1
        groupExpensesResponse.expenses.first().also {
            it.creatorId shouldBe expense.creatorId
            it.title shouldBe expense.title
            it.totalCost shouldBe expense.totalCost
            it.baseCurrency shouldBe expense.baseCurrency
            it.targetCurrency shouldBe expense.targetCurrency
            it.exchangeRate shouldBe expense.exchangeRate?.value
            it.participants.shouldHaveSize(1)
            it.participants.first().also { participant ->
                participant.participantId shouldBe expense.expenseParticipants.first().participantId
                participant.participantCost shouldBe expense.expenseParticipants.first().participantCost
            }
            it.expenseDate shouldBe expense.expenseDate
        }
    }

    should("map multiple Expenses to AcceptedGroupExpensesResponse") {
        // given
        val creatorIds = listOf("creatorId1", "creatorId2", "creatorId3")
        val titles = listOf("title1", "title2", "title3")
        val costs = listOf(BigDecimal.ONE, BigDecimal.TWO, BigDecimal.TEN)
        val baseCurrencies = listOf("PLN", "EUR", "USD")
        val targetCurrencies = listOf(null, "PLN", "EUR")
        val exchangeRates = listOf(null, BigDecimal.TWO, BigDecimal.TWO)
        val participants = listOf(
            createListOfAcceptedGroupExpenseParticipantDto(
                listOf("userId1", "userId2", "userId3"),
                listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
            ),
            createListOfAcceptedGroupExpenseParticipantDto(
                listOf("userId3", "userId1", "userId2"),
                listOf(BigDecimal("20"), BigDecimal("30"), BigDecimal("10")),
            ),
            createListOfAcceptedGroupExpenseParticipantDto(
                listOf("userId2", "userId3", "userId1"),
                listOf(BigDecimal("30"), BigDecimal("10"), BigDecimal("20")),
            ),
        )
        val expenseDates = listOf(
            Instant.ofEpochSecond(1000),
            Instant.ofEpochSecond(2000),
            Instant.ofEpochSecond(3000),
        )
        val expenses = creatorIds.mapIndexed { index, creatorId ->
            createExpense(
                creatorId = creatorId,
                title = titles[index],
                totalCost = costs[index],
                baseCurrency = baseCurrencies[index],
                targetCurrency = targetCurrencies[index],
                exchangeRate = exchangeRates[index],
                expenseParticipants = participants[index].map { createExpenseParticipant(it.participantId, it.participantCost) },
                expenseDate = expenseDates[index],
            )
        }

        // when
        val groupExpensesResponse = expenses.toAcceptedGroupExpensesResponse(GROUP_ID)

        // then
        groupExpensesResponse.groupId shouldBe GROUP_ID
        groupExpensesResponse.expenses.also {
            it shouldHaveSize 3
            it.map { groupExpensesDto -> groupExpensesDto.creatorId } shouldContainExactly creatorIds
            it.map { groupExpensesDto -> groupExpensesDto.title } shouldContainExactly titles
            it.map { groupExpensesDto -> groupExpensesDto.totalCost } shouldContainExactly costs
            it.map { groupExpensesDto -> groupExpensesDto.baseCurrency } shouldContainExactly baseCurrencies
            it.map { groupExpensesDto -> groupExpensesDto.targetCurrency } shouldContainExactly targetCurrencies
            it.map { groupExpensesDto -> groupExpensesDto.exchangeRate } shouldContainExactly exchangeRates
            it.map { groupExpensesDto -> groupExpensesDto.participants } shouldContainExactly participants
            it.map { groupExpensesDto -> groupExpensesDto.expenseDate } shouldContainExactly expenseDates
        }
    }

    should("return empty list when there are no expenses") {
        // given
        val expenses = listOf<Expense>()

        // when
        val groupExpensesResponse = expenses.toAcceptedGroupExpensesResponse(GROUP_ID)

        // then
        groupExpensesResponse.also {
            it.groupId shouldBe GROUP_ID
            it.expenses shouldBe listOf()
        }
    }
},)
