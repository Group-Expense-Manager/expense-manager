package pl.edu.agh.gem.external.dto.expense

import pl.edu.agh.gem.internal.model.expense.UserExpense
import java.math.BigDecimal

data class UserExpensesResponse(
    val expenses: List<UserExpenseDto>,
)

data class UserExpenseDto(
    val value: BigDecimal,
    val currency: String,
    val exchangeRate: BigDecimal?,

)

private fun UserExpense.toUserExpenseDto() = UserExpenseDto(
    value = value,
    currency = currency,
    exchangeRate = exchangeRate?.value,
)

fun List<UserExpense>.toUserExpensesResponse() = UserExpensesResponse(
    expenses = map { it.toUserExpenseDto() },
)
