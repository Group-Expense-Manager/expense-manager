package pl.edu.agh.gem.internal.mapper

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.util.DummyData.CURRENCY_1
import pl.edu.agh.gem.util.DummyData.CURRENCY_2
import pl.edu.agh.gem.util.DummyData.EXCHANGE_RATE_VALUE
import pl.edu.agh.gem.util.Triple
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createExpenseParticipants
import pl.edu.agh.gem.util.createUserExpense
import java.math.BigDecimal

class DebtorUserExpenseMapperTest : ShouldSpec({

    val debtorUserExpenseMapper = DebtorUserExpenseMapper()

    context("map correctly") {
        withData(
            Triple(
                OTHER_USER_ID,
                createExpense(
                    creatorId = USER_ID,
                    totalCost = BigDecimal("60"),
                    baseCurrency = CURRENCY_1,
                    targetCurrency = null,
                    exchangeRate = null,
                    expenseParticipants = createExpenseParticipants(
                        listOf(USER_ID, OTHER_USER_ID, "userId3"),
                        listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
                    ),
                    status = ACCEPTED,
                ),
                createUserExpense(BigDecimal("-20"), CURRENCY_1),
            ),
            Triple(
                OTHER_USER_ID,
                createExpense(
                    creatorId = USER_ID,
                    totalCost = BigDecimal("60"),
                    baseCurrency = CURRENCY_1,
                    targetCurrency = CURRENCY_2,
                    exchangeRate = EXCHANGE_RATE_VALUE,
                    expenseParticipants = createExpenseParticipants(
                        listOf(USER_ID, OTHER_USER_ID, "userId3"),
                        listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
                    ),
                    status = ACCEPTED,
                ),
                createUserExpense(BigDecimal("-20"), CURRENCY_2, EXCHANGE_RATE_VALUE),
            ),
            Triple(
                OTHER_USER_ID,
                createExpense(
                    creatorId = USER_ID,
                    totalCost = BigDecimal("60"),
                    baseCurrency = CURRENCY_1,
                    targetCurrency = null,
                    exchangeRate = null,
                    expenseParticipants = createExpenseParticipants(
                        listOf(USER_ID, OTHER_USER_ID, "userId3"),
                        listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
                    ),
                    status = PENDING,
                ),
                null,
            ),
            Triple(
                USER_ID,
                createExpense(
                    creatorId = USER_ID,
                    totalCost = BigDecimal("60"),
                    baseCurrency = CURRENCY_1,
                    targetCurrency = null,
                    exchangeRate = null,
                    expenseParticipants = createExpenseParticipants(
                        listOf(USER_ID, OTHER_USER_ID, "userId3"),
                        listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
                    ),
                    status = ACCEPTED,
                ),
                null,
            ),
            Triple(
                "notExpenseParticipantId",
                createExpense(
                    creatorId = USER_ID,
                    totalCost = BigDecimal("60"),
                    baseCurrency = CURRENCY_1,
                    targetCurrency = null,
                    exchangeRate = null,
                    expenseParticipants = createExpenseParticipants(
                        listOf(USER_ID, "userId2", "userId3"),
                        listOf(BigDecimal("10"), BigDecimal("20"), BigDecimal("30")),
                    ),
                    status = ACCEPTED,
                ),
                null,
            ),

        ) { (userId, expense, expectedUserExpense) ->
            // when
            val actualUserExpense = debtorUserExpenseMapper.mapToUserExpense(userId, expense)

            // then
            actualUserExpense shouldBe expectedUserExpense
        }
    }
},)
