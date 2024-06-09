package pl.edu.agh.gem.external.dto.expense

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.model.expense.ExpenseAction.CREATED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.util.createExpenseCreationRequest
import pl.edu.agh.gem.util.createExpenseParticipantDto

class ExpenseCreationRequestTest : ShouldSpec({

    should("Map to domain") {
        // given
        val expenseParticipants = arrayListOf(createExpenseParticipantDto(participantId = USER_ID))
        val expenseCreationRequest = createExpenseCreationRequest(expenseParticipants = expenseParticipants)

        // when
        val expense = expenseCreationRequest.toDomain(USER_ID, GROUP_ID)

        // then
        expense.shouldNotBeNull()
        expense.also {
            it.id.shouldNotBeNull()
            it.groupId shouldBe GROUP_ID
            it.creatorId shouldBe USER_ID
            it.cost shouldBe expenseCreationRequest.cost
            it.baseCurrency shouldBe expenseCreationRequest.baseCurrency
            it.targetCurrency shouldBe expenseCreationRequest.targetCurrency
            it.exchangeRate.shouldBeNull()
            it.createdAt.shouldNotBeNull()
            it.updatedAt.shouldNotBeNull()
            it.expenseDate shouldBe expenseCreationRequest.expenseDate
            it.attachmentId shouldBe expenseCreationRequest.attachmentId
            it.expenseParticipants shouldHaveSize 1
            it.expenseParticipants.first().also { participant ->
                participant.participantId shouldBe expenseParticipants.first().participantId
                participant.participantCost shouldBe expenseParticipants.first().participantCost
                participant.participantStatus shouldBe ACCEPTED
            }
            it.status shouldBe PENDING
            it.statusHistory shouldHaveSize 1
            it.statusHistory.first().also { entry ->
                entry.createdAt.shouldNotBeNull()
                entry.expenseAction shouldBe CREATED
                entry.participantId shouldBe USER_ID
                entry.comment shouldBe expenseCreationRequest.message
            }
        }
    }
},)
