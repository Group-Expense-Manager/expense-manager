package pl.edu.agh.gem.internal.model

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.model.expense.ExpenseAction.CREATED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.util.DummyData.EXCHANGE_RATE_VALUE
import pl.edu.agh.gem.util.createExpenseCreation
import pl.edu.agh.gem.util.createExpenseParticipantCost
import pl.edu.agh.gem.util.createExpenseParticipantDto
import pl.edu.agh.gem.util.createFxData

class ExpenseCreationTest : ShouldSpec({

    should("Map to Expense") {
        // given
        val expenseParticipants = arrayListOf(createExpenseParticipantCost(participantId = USER_ID))
        val expenseCreation = createExpenseCreation(expenseParticipantsCost = expenseParticipants)

        // when
        val expense = expenseCreation.toExpense(createFxData())

        // then
        expense.shouldNotBeNull()
        expense.also {
            it.id.shouldNotBeNull()
            it.groupId shouldBe GROUP_ID
            it.creatorId shouldBe USER_ID
            it.title shouldBe expenseCreation.title
            it.amount shouldBe expenseCreation.amount
            it.fxData?.targetCurrency shouldBe expenseCreation.targetCurrency
            it.fxData?.exchangeRate shouldBe EXCHANGE_RATE_VALUE
            it.createdAt.shouldNotBeNull()
            it.updatedAt.shouldNotBeNull()
            it.expenseDate shouldBe expenseCreation.expenseDate
            it.attachmentId shouldBe expenseCreation.attachmentId
            it.expenseParticipants shouldHaveSize 1
            it.expenseParticipants.first().also { participant ->
                participant.participantId shouldBe expenseParticipants.first().participantId
                participant.participantCost shouldBe expenseParticipants.first().participantCost
                participant.participantStatus shouldBe PENDING
            }
            it.status shouldBe PENDING
            it.history shouldHaveSize 1
            it.history.first().also { entry ->
                entry.createdAt.shouldNotBeNull()
                entry.expenseAction shouldBe CREATED
                entry.participantId shouldBe USER_ID
                entry.comment shouldBe expenseCreation.message
            }
        }
    }

    should("map expense createExpenseParticipantDto to domain") {
        // given
        val expenseParticipantDto = createExpenseParticipantDto(OTHER_USER_ID)

        // when
        val expenseParticipant = expenseParticipantDto.toDomain()

        // then
        expenseParticipant.also {
            it.participantId shouldBe OTHER_USER_ID
            it.participantCost shouldBe expenseParticipantDto.participantCost
            it.participantStatus shouldBe PENDING
        }
    }
},)
