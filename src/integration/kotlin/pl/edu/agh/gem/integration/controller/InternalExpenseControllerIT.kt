package pl.edu.agh.gem.integration.controller

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.assertion.shouldBody
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.external.dto.expense.UserExpensesResponse
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.util.DummyData.CURRENCY_1
import pl.edu.agh.gem.util.DummyData.CURRENCY_2
import pl.edu.agh.gem.util.DummyData.EXCHANGE_RATE_VALUE
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createExpenseParticipants
import java.math.BigDecimal

class InternalExpenseControllerIT(
    private val service: ServiceTestClient,
    private val repository: ExpenseRepository,
) : BaseIntegrationSpec({

    should("get user expenses") {
        // given
        val expenseList = listOf(
            createExpense(
                id = "1",
                creatorId = USER_ID,
                cost = BigDecimal("60"),
                baseCurrency = CURRENCY_1,
                targetCurrency = null,
                exchangeRate = null,
                expenseParticipants = createExpenseParticipants(
                    listOf(USER_ID, "userId2", "userId3"),
                    listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
                ),
                status = ACCEPTED,
            ),

            createExpense(
                id = "2",
                creatorId = OTHER_USER_ID,
                cost = BigDecimal("60"),
                baseCurrency = CURRENCY_1,
                targetCurrency = CURRENCY_2,
                exchangeRate = EXCHANGE_RATE_VALUE,
                expenseParticipants = createExpenseParticipants(
                    listOf(USER_ID, OTHER_USER_ID, "userId3"),
                    listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
                ),
                status = ACCEPTED,
            ),

        )
        expenseList.forEach { repository.save(it) }

        // when
        val response = service.getUserExpenses(GROUP_ID, USER_ID)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<UserExpensesResponse> {
            userId shouldBe USER_ID
            expenses.size shouldBe 2
            expenses.first().also { userExpenses ->
                userExpenses.value shouldBe BigDecimal("50")
                userExpenses.currency shouldBe CURRENCY_1
                userExpenses.exchangeRate.shouldBeNull()
            }

            expenses.last().also { userExpenses ->
                userExpenses.value shouldBe BigDecimal("-10")
                userExpenses.currency shouldBe CURRENCY_2
                userExpenses.exchangeRate shouldBe EXCHANGE_RATE_VALUE
            }
        }
    }
},)
