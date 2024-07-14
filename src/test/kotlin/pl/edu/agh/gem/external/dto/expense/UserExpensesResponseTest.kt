package pl.edu.agh.gem.external.dto.expense

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.util.createUserExpense

class UserExpensesResponseTest : ShouldSpec({
    should("map to UserExpensesResponse") {
        // given
        val userExpense = createUserExpense()

        // when
        val userExpensesResponse = listOf(userExpense).toUserExpensesResponse(USER_ID)

        // then
        userExpensesResponse.also {
            it.userId shouldBe USER_ID
            it.expenses shouldHaveSize 1
            it.expenses.first().also { expense ->
                expense.value shouldBe userExpense.value
                expense.currency shouldBe userExpense.currency
                expense.exchangeRate shouldBe userExpense.exchangeRate
            }
        }
    }
},)
