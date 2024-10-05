package pl.edu.agh.gem.internal.mapper

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.UserExpense
import java.math.BigDecimal

class CreditorUserExpenseMapper {
    fun mapToUserExpense(creditorId: String, expense: Expense): UserExpense? =
        when {
            expense.status == ACCEPTED && creditorId == expense.creatorId -> UserExpense(
                value = getCostAsExpenseCreator(expense),
                currency = expense.fxData?.targetCurrency ?: expense.amount.currency,
                exchangeRate = expense.fxData?.exchangeRate,
            )
            else -> null
        }

    private fun getCostAsExpenseCreator(expense: Expense): BigDecimal =
        expense.expenseParticipants.filterNot { it.participantId == expense.creatorId }
            .sumOf { it.participantCost }
}
