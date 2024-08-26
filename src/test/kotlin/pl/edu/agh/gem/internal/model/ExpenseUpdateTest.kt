package pl.edu.agh.gem.internal.model

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.external.dto.expense.toExpenseParticipantCost
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.toExpenseParticipantCost
import pl.edu.agh.gem.util.createExpenseParticipant
import pl.edu.agh.gem.util.createExpenseParticipantCost
import pl.edu.agh.gem.util.createExpenseParticipantDto

class ExpenseUpdateTest : ShouldSpec({
    should("map ExpenseParticipantCost to ExpenseParticipant") {
        // given
        val expenseUpdateParticipant = createExpenseParticipantCost()

        // when
        val expenseParticipant = expenseUpdateParticipant.toExpenseParticipant(USER_ID)

        // then
        expenseParticipant.also {
            it.participantId shouldBe expenseUpdateParticipant.participantId
            it.participantCost shouldBe expenseUpdateParticipant.participantCost
            it.participantStatus shouldBe ACCEPTED
        }
    }

    should("map ExpenseParticipant to ExpenseParticipantCost") {
        // given
        val expenseParticipant = createExpenseParticipant()

        // when
        val expenseUpdateParticipant = expenseParticipant.toExpenseParticipantCost()

        // then
        expenseUpdateParticipant.also {
            it.participantId shouldBe expenseParticipant.participantId
            it.participantCost shouldBe expenseParticipant.participantCost
        }
    }

    should("map ExpenseParticipantRequestData to ExpenseParticipantCost") {
        // given
        val expenseParticipantRequestData = createExpenseParticipantDto()

        // when
        val expenseUpdateParticipant = expenseParticipantRequestData.toExpenseParticipantCost()

        // then
        expenseUpdateParticipant.also {
            it.participantId shouldBe expenseParticipantRequestData.participantId
            it.participantCost shouldBe expenseParticipantRequestData.participantCost
        }
    }
},)
