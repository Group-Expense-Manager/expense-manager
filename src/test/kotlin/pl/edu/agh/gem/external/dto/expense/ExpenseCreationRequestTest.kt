package pl.edu.agh.gem.external.dto.expense

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.model.expense.ExpenseAction.CREATED
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
        expense shouldNotBe null
        expense.also {
            it.id shouldNotBe null
            it.groupId shouldBe GROUP_ID
            it.creatorId shouldBe USER_ID
            it.cost shouldBe expenseCreationRequest.cost
            it.baseCurrency shouldBe expenseCreationRequest.baseCurrency
            it.targetCurrency shouldBe expenseCreationRequest.targetCurrency
            it.exchangeRate shouldBe null
            it.createdAt shouldNotBe null
            it.updatedAt shouldNotBe null
            it.expenseDate shouldBe expenseCreationRequest.expenseDate
            it.attachmentId shouldBe expenseCreationRequest.attachmentId
            it.expenseParticipants shouldHaveSize 1
            it.expenseParticipants.first().also { participant ->
                participant.participantId shouldBe expenseParticipants.first().participantId
                participant.participantCost shouldBe expenseParticipants.first().participantCost
                participant.participantStatus shouldBe PENDING
            }
            it.status shouldBe PENDING
            it.statusHistory shouldHaveSize 1
            it.statusHistory.first().also { entry ->
                entry.createdAt shouldNotBe null
                entry.expenseAction shouldBe CREATED
                entry.participantId shouldBe USER_ID
                entry.comment shouldBe null
            }
        }
    }
},)
