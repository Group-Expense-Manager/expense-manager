package pl.edu.agh.gem.external.dto.expense

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.util.createUserExpense

class UserExpensesResponseTest : ShouldSpec({
    should("map to UserExpensesResponse") {
        // given
        val userExpense = createUserExpense()

        // when
        val userExpensesResponse = listOf(userExpense).toUserExpensesResponse()

        // then
        userExpensesResponse.expenses shouldHaveSize 1
        userExpensesResponse.expenses.first().also {
            it.value shouldBe userExpense.value
            it.currency shouldBe userExpense.currency
            it.exchangeRate shouldBe userExpense.exchangeRate
        }
    }
},)
