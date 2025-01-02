package pl.edu.agh.gem.external.dto.expense

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.util.createExpenseDecisionRequest

class ExpenseDecisionRequestTest : ShouldSpec({
    should("Map correctly to Decision") {
        // given
        val expenseDecision = createExpenseDecisionRequest()

        // when
        val result = expenseDecision.toDomain(USER_ID)

        // then
        result shouldNotBe null
        result.also {
            it.expenseId shouldBe expenseDecision.expenseId
            it.groupId shouldBe expenseDecision.groupId
            it.decision shouldBe expenseDecision.decision
            it.message shouldBe expenseDecision.message
        }
    }
})
