package pl.edu.agh.gem.external.dto.expense

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createExpenseParticipant

class ExpenseResponseTest : ShouldSpec({

    should("map from domain") {
        // given
        val expense = createExpense(expenseParticipants = listOf(createExpenseParticipant()))

        // when
        val expenseResponse = ExpenseResponse.fromExpense(expense)

        // then
        expenseResponse shouldNotBe null
        expenseResponse.also {
            it.creatorId shouldBe expense.creatorId
            it.amount shouldBe expense.amount.toAmountDto()
            it.fxData
            it.createdAt shouldBe expense.createdAt
            it.updatedAt shouldBe expense.updatedAt
            it.expenseDate shouldBe expense.expenseDate
            it.attachmentId shouldBe expense.attachmentId
            it.expenseParticipants shouldHaveSize 1
            it.expenseParticipants.first().also { participant ->
                participant.participantId shouldBe expense.expenseParticipants.first().participantId
                participant.participantCost shouldBe expense.expenseParticipants.first().participantCost
                participant.participantStatus shouldBe expense.expenseParticipants.first().participantStatus.name
            }
            it.status shouldBe expense.status.name
            it.history shouldHaveSize 1
            it.history.first().also { entry ->
                entry.createdAt shouldBe expense.history.first().createdAt
                entry.expenseAction shouldBe expense.history.first().expenseAction.name
                entry.participantId shouldBe expense.history.first().participantId
                entry.comment shouldBe expense.history.first().comment
            }
        }
    }
},)
