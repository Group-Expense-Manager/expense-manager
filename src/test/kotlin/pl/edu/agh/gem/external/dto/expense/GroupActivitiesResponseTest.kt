package pl.edu.agh.gem.external.dto.expense

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.internal.model.expense.Amount
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.REJECTED
import pl.edu.agh.gem.internal.model.expense.FxData
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createExpenseParticipant
import java.math.BigDecimal
import java.time.Instant

class GroupActivitiesResponseTest : ShouldSpec({

    should("map Expense to GroupActivitiesResponse") {
        // given
        val expense = createExpense(expenseParticipants = listOf(createExpenseParticipant()))

        // when
        val groupActivitiesResponse = listOf(expense).toGroupActivitiesResponse(GROUP_ID)

        // then
        groupActivitiesResponse.expenses shouldHaveSize 1
        groupActivitiesResponse.expenses.first().also {
            it.expenseId shouldBe expense.id
            it.creatorId shouldBe expense.creatorId
            it.title shouldBe expense.title
            it.amount shouldBe expense.amount.toAmountDto()
            it.fxData shouldBe expense.fxData?.toDto()
            it.status shouldBe expense.status
            it.participantIds.shouldHaveSize(1)
            it.participantIds.first() shouldBe expense.expenseParticipants.first().participantId
            it.expenseDate shouldBe expense.expenseDate
        }
    }

    should("map multiple Expenses to GroupActivitiesResponse") {
        // given
        val expenseIds = listOf("expenseId1", "expenseId2", "expenseId3")
        val creatorIds = listOf("creatorId1", "creatorId2", "creatorId3")
        val titles = listOf("title1", "title2", "title3")
        val amounts =
            listOf(
                Amount(value = BigDecimal.ONE, currency = "PLN"),
                Amount(value = BigDecimal.TWO, currency = "EUR"),
                Amount(value = BigDecimal.TEN, currency = "USD"),
            )
        val fxData =
            listOf(
                FxData(targetCurrency = "EUR", exchangeRate = "2".toBigDecimal()),
                null,
                FxData(targetCurrency = "PLN", exchangeRate = "3".toBigDecimal()),
            )
        val statuses = listOf(PENDING, ACCEPTED, REJECTED)
        val participantIds =
            listOf(
                listOf("participant1", "participant2"),
                listOf("participant3", "participant4"),
                listOf("participant5", "participant6"),
            )
        val expenseDates =
            listOf(
                Instant.ofEpochSecond(1000),
                Instant.ofEpochSecond(2000),
                Instant.ofEpochSecond(3000),
            )
        val expenses =
            expenseIds.mapIndexed { index, expenseId ->
                createExpense(
                    id = expenseId,
                    creatorId = creatorIds[index],
                    title = titles[index],
                    amount = amounts[index],
                    fxData = fxData[index],
                    status = statuses[index],
                    expenseParticipants = participantIds[index].map { createExpenseParticipant(participantId = it) },
                    expenseDate = expenseDates[index],
                )
            }

        // when
        val groupActivitiesResponse = expenses.toGroupActivitiesResponse(GROUP_ID)

        // then
        groupActivitiesResponse.groupId shouldBe GROUP_ID
        groupActivitiesResponse.expenses.also {
            it shouldHaveSize 3
            it.map { groupExpensesDto -> groupExpensesDto.expenseId } shouldContainExactly expenseIds
            it.map { groupExpensesDto -> groupExpensesDto.creatorId } shouldContainExactly creatorIds
            it.map { groupExpensesDto -> groupExpensesDto.title } shouldContainExactly titles
            it.map { groupPaymentsDto -> groupPaymentsDto.amount } shouldContainExactly amounts.map { amount -> amount.toAmountDto() }
            it.map { groupPaymentsDto -> groupPaymentsDto.fxData } shouldContainExactly fxData.map { fxData -> fxData?.toDto() }
            it.map { groupExpensesDto -> groupExpensesDto.status } shouldContainExactly statuses
            it.map { groupExpensesDto -> groupExpensesDto.participantIds } shouldContainExactly participantIds
            it.map { groupExpensesDto -> groupExpensesDto.expenseDate } shouldContainExactly expenseDates
        }
    }

    should("return empty list when there are no expenses") {
        // given
        val expenses = listOf<Expense>()

        // when
        val groupActivitiesResponse = expenses.toGroupActivitiesResponse(GROUP_ID)

        // then
        groupActivitiesResponse.also {
            it.groupId shouldBe GROUP_ID
            it.expenses shouldBe listOf()
        }
    }
})
