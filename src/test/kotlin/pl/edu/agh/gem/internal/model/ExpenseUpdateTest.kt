package pl.edu.agh.gem.internal.model

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.toExpenseUpdateParticipant
import pl.edu.agh.gem.util.createExpenseParticipant
import pl.edu.agh.gem.util.createExpenseParticipantDto
import pl.edu.agh.gem.util.createExpenseUpdateParticipant

class ExpenseUpdateTest : ShouldSpec({
    should("map ExpenseUpdateParticipant to ExpenseParticipant") {
        // given
        val expenseUpdateParticipant = createExpenseUpdateParticipant()

        // when
        val expenseParticipant = expenseUpdateParticipant.toExpenseParticipant()

        // then
        expenseParticipant.also {
            it.participantId shouldBe expenseUpdateParticipant.participantId
            it.participantCost shouldBe expenseUpdateParticipant.participantCost
            it.participantStatus shouldBe PENDING
        }
    }

    should("map ExpenseParticipant to ExpenseUpdateParticipant") {
        // given
        val expenseParticipant = createExpenseParticipant()

        // when
        val expenseUpdateParticipant = expenseParticipant.toExpenseUpdateParticipant()

        // then
        expenseUpdateParticipant.also {
            it.participantId shouldBe expenseParticipant.participantId
            it.participantCost shouldBe expenseParticipant.participantCost
        }
    }

    should("map ExpenseParticipantRequestData to ExpenseUpdateParticipant") {
        // given
        val expenseParticipantRequestData = createExpenseParticipantDto()

        // when
        val expenseUpdateParticipant = expenseParticipantRequestData.toExpenseUpdateParticipant()

        // then
        expenseUpdateParticipant.also {
            it.participantId shouldBe expenseParticipantRequestData.participantId
            it.participantCost shouldBe expenseParticipantRequestData.participantCost
        }
    }
},)
