package pl.edu.agh.gem.internal.mapper

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.UserExpense

class DebtorUserExpenseMapper {
    fun mapToUserExpense(debtorId: String, expense: Expense): UserExpense? {
        if (expense.status != ACCEPTED) return null
        if (expense.creatorId == debtorId) return null
        val cost = expense.expenseParticipants.find { it.participantId == debtorId }?.participantCost ?: return null

        return UserExpense(
            value = cost.negate(),
            currency = expense.targetCurrency ?: expense.baseCurrency,
            exchangeRate = expense.exchangeRate,
        )
    }
}
