package pl.edu.agh.gem.internal.model

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.model.expense.ExpenseAction.CREATED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.util.DummyData.ATTACHMENT_ID
import pl.edu.agh.gem.util.createExchangeRate
import pl.edu.agh.gem.util.createExpenseCreation
import pl.edu.agh.gem.util.createExpenseParticipantCost
import pl.edu.agh.gem.util.createExpenseParticipantDto

class ExpenseCreationTest : ShouldSpec({

    should("Map to Expense") {
        // given
        val expenseParticipants = arrayListOf(createExpenseParticipantCost(participantId = USER_ID))
        val expenseCreation = createExpenseCreation(expenseParticipantsCost = expenseParticipants)
        val exchangeRate = createExchangeRate()

        // when
        val expense = expenseCreation.toExpense(exchangeRate, ATTACHMENT_ID)

        // then
        expense.shouldNotBeNull()
        expense.also {
            it.id.shouldNotBeNull()
            it.groupId shouldBe GROUP_ID
            it.creatorId shouldBe USER_ID
            it.title shouldBe expenseCreation.title
            it.cost shouldBe expenseCreation.cost
            it.baseCurrency shouldBe expenseCreation.baseCurrency
            it.targetCurrency shouldBe expenseCreation.targetCurrency
            it.exchangeRate shouldBe exchangeRate
            it.createdAt.shouldNotBeNull()
            it.updatedAt.shouldNotBeNull()
            it.expenseDate shouldBe expenseCreation.expenseDate
            it.attachmentId shouldBe expenseCreation.attachmentId
            it.expenseParticipants shouldHaveSize 1
            it.expenseParticipants.first().also { participant ->
                participant.participantId shouldBe expenseParticipants.first().participantId
                participant.participantCost shouldBe expenseParticipants.first().participantCost
                participant.participantStatus shouldBe ACCEPTED
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

    should("map expense creator ExpenseParticipantRequestData to domain") {
        // given
        val expenseParticipantRequestData = createExpenseParticipantDto(USER_ID)

        // when
        val expenseParticipant = expenseParticipantRequestData.toDomain(USER_ID)

        // then
        expenseParticipant.also {
            it.participantId shouldBe USER_ID
            it.participantCost shouldBe expenseParticipantRequestData.participantCost
            it.participantStatus shouldBe ACCEPTED
        }
    }

    should("map expense participant ExpenseParticipantRequestData to domain") {
        // given
        val expenseParticipantRequestData = createExpenseParticipantDto(OTHER_USER_ID)

        // when
        val expenseParticipant = expenseParticipantRequestData.toDomain(USER_ID)

        // then
        expenseParticipant.also {
            it.participantId shouldBe OTHER_USER_ID
            it.participantCost shouldBe expenseParticipantRequestData.participantCost
            it.participantStatus shouldBe PENDING
        }
    }
},)
