package pl.edu.agh.gem.external.dto.expense

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.util.DummyData.EXPENSE_ID
import pl.edu.agh.gem.util.createExpenseParticipantDto
import pl.edu.agh.gem.util.createExpenseUpdateRequest

class ExpenseUpdateRequestTest : ShouldSpec({

    should("Map to domain correctly") {
        // given
        val expenseParticipants = arrayListOf(createExpenseParticipantDto(participantId = USER_ID))
        val expenseUpdateRequest = createExpenseUpdateRequest(expenseParticipants = expenseParticipants)

        // when
        val expenseUpdate = expenseUpdateRequest.toDomain(EXPENSE_ID, GROUP_ID, USER_ID)

        // then
        expenseUpdate.also {
            it.id shouldBe EXPENSE_ID
            it.groupId shouldBe GROUP_ID
            it.userId shouldBe USER_ID
            it.title shouldBe expenseUpdateRequest.title
            it.cost shouldBe expenseUpdateRequest.cost
            it.baseCurrency shouldBe expenseUpdateRequest.baseCurrency
            it.targetCurrency shouldBe expenseUpdateRequest.targetCurrency
            it.expenseDate shouldBe expenseUpdateRequest.expenseDate
            it.expenseParticipantsCost shouldHaveSize 1
            it.expenseParticipantsCost.first().also { participant ->
                participant.participantId shouldBe expenseParticipants.first().participantId
                participant.participantCost shouldBe expenseParticipants.first().participantCost
            }
            it.message shouldBe expenseUpdateRequest.message
        }
    }
},)
