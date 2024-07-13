package pl.edu.agh.gem.external.dto.expense

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.util.createExpense

class ExpenseUpdateResponseTest : ShouldSpec({

    should("map expense to expenseUpdateResponse correctly") {
        // given
        val expense = createExpense()

        // when
        val expenseUpdateResponse = expense.toExpenseUpdateResponse()

        // then
        expenseUpdateResponse.expenseId shouldBe expense.id
    }
},)
