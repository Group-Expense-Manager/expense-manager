package pl.edu.agh.gem.internal.mapper

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.UserExpense

class DebtorUserExpenseMapper {
    fun mapToUserExpense(
        debtorId: String,
        expense: Expense,
    ): UserExpense? {
        val cost = expense.expenseParticipants.find { it.participantId == debtorId }?.participantCost
        return when {
            expense.status == ACCEPTED && expense.creatorId != debtorId && cost != null -> {
                UserExpense(
                    value = cost.negate(),
                    currency = expense.fxData?.targetCurrency ?: expense.amount.currency,
                    exchangeRate = expense.fxData?.exchangeRate,
                )
            }
            else -> null
        }
    }
}
