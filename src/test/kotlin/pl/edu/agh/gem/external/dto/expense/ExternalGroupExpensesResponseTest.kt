package pl.edu.agh.gem.external.dto.expense

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.REJECTED
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createExpenseParticipant
import java.math.BigDecimal
import java.time.Instant

class ExternalGroupExpensesResponseTest : ShouldSpec({

    should("map Expense to ExternalGroupExpensesResponse") {
        // given
        val expense = createExpense(expenseParticipants = listOf(createExpenseParticipant()))

        // when
        val groupExpensesResponse = listOf(expense).toExternalGroupExpensesResponse()

        // then
        groupExpensesResponse.expenses shouldHaveSize 1
        groupExpensesResponse.expenses.first().also {
            it.expenseId shouldBe expense.id
            it.creatorId shouldBe expense.creatorId
            it.title shouldBe expense.title
            it.cost shouldBe expense.cost
            it.baseCurrency shouldBe expense.baseCurrency
            it.status shouldBe expense.status.name
            it.participantIds.shouldHaveSize(1)
            it.participantIds.first() shouldBe expense.expenseParticipants.first().participantId
            it.expenseDate shouldBe expense.expenseDate
        }
    }

    should("map multiple Expenses to ExternalGroupExpensesResponse") {
        // given
        val expenseIds = listOf("expenseId1", "expenseId2", "expenseId3")
        val creatorIds = listOf("creatorId1", "creatorId2", "creatorId3")
        val titles = listOf("title1", "title2", "title3")
        val costs = listOf(BigDecimal.ONE, BigDecimal.TWO, BigDecimal.TEN)
        val baseCurrencies = listOf("PLN", "EUR", "USD")
        val statuses = listOf(PENDING, ACCEPTED, REJECTED)
        val participantIds = listOf(
            listOf("participant1", "participant2"),
            listOf("participant3", "participant4"),
            listOf("participant5", "participant6"),
        )
        val expenseDates = listOf(
            Instant.ofEpochSecond(1000),
            Instant.ofEpochSecond(2000),
            Instant.ofEpochSecond(3000),
        )
        val expenses = expenseIds.mapIndexed { index, expenseId ->
            createExpense(
                id = expenseId,
                creatorId = creatorIds[index],
                title = titles[index],
                cost = costs[index],
                baseCurrency = baseCurrencies[index],
                status = statuses[index],
                expenseParticipants = participantIds[index].map { createExpenseParticipant(participantId = it) },
                expenseDate = expenseDates[index],
            )
        }

        // when
        val groupExpensesResponse = expenses.toExternalGroupExpensesResponse()

        // then
        groupExpensesResponse.expenses.also {
            it shouldHaveSize 3
            it.map { groupExpensesDto -> groupExpensesDto.expenseId } shouldContainExactly expenseIds
            it.map { groupExpensesDto -> groupExpensesDto.creatorId } shouldContainExactly creatorIds
            it.map { groupExpensesDto -> groupExpensesDto.title } shouldContainExactly titles
            it.map { groupExpensesDto -> groupExpensesDto.cost } shouldContainExactly costs
            it.map { groupExpensesDto -> groupExpensesDto.baseCurrency } shouldContainExactly baseCurrencies
            it.map { groupExpensesDto -> groupExpensesDto.status } shouldContainExactly statuses.map { status -> status.name }
            it.map { groupExpensesDto -> groupExpensesDto.participantIds } shouldContainExactly participantIds
            it.map { groupExpensesDto -> groupExpensesDto.expenseDate } shouldContainExactly expenseDates
        }
    }

    should("return empty list when there are no expenses") {
        // given
        val expenses = listOf<Expense>()

        // when
        val groupExpensesResponse = expenses.toExternalGroupExpensesResponse()

        // then
        groupExpensesResponse.expenses shouldBe listOf()
    }
},)
