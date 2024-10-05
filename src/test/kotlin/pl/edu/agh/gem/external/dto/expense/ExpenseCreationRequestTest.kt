package pl.edu.agh.gem.external.dto.expense

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.util.createExpenseCreationRequest
import pl.edu.agh.gem.util.createExpenseParticipantDto

class ExpenseCreationRequestTest : ShouldSpec({

    should("Map to domain") {
        // given
        val expenseParticipants = arrayListOf(createExpenseParticipantDto(participantId = USER_ID))
        val expenseCreationRequest = createExpenseCreationRequest(expenseParticipants = expenseParticipants)

        // when
        val expenseCreation = expenseCreationRequest.toDomain(USER_ID, GROUP_ID)

        // then
        expenseCreation.shouldNotBeNull()
        expenseCreation.also {
            it.groupId shouldBe GROUP_ID
            it.creatorId shouldBe USER_ID
            it.title shouldBe expenseCreationRequest.title
            it.amount shouldBe expenseCreationRequest.amount.toDomain()
            it.targetCurrency shouldBe expenseCreationRequest.targetCurrency
            it.expenseDate shouldBe expenseCreationRequest.expenseDate
            it.attachmentId shouldBe expenseCreationRequest.attachmentId
            it.expenseParticipantsCost shouldHaveSize 1
            it.expenseParticipantsCost.first().also { participant ->
                participant.participantId shouldBe expenseParticipants.first().participantId
                participant.participantCost shouldBe expenseParticipants.first().participantCost
            }
            it.message shouldBe expenseCreationRequest.message
        }
    }

    should("map ExpenseParticipantRequestData to domain") {
        // given
        val expenseParticipantRequestData = createExpenseParticipantDto(OTHER_USER_ID)

        // when
        val expenseParticipant = expenseParticipantRequestData.toDomain()

        // then
        expenseParticipant.also {
            it.participantId shouldBe OTHER_USER_ID
            it.participantCost shouldBe expenseParticipantRequestData.participantCost
            it.participantStatus shouldBe PENDING
        }
    }
},)
