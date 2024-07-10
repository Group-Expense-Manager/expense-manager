package pl.edu.agh.gem.internal.mapper

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.UserExpense
import java.math.BigDecimal

class CreditorUserExpenseMapper {
    fun mapToUserExpense(creditorId: String, expense: Expense): UserExpense? {
        if (expense.status != ACCEPTED) return null
        if (creditorId != expense.creatorId) return null
        return UserExpense(
            value = getCostAsExpenseCreator(expense),
            currency = expense.targetCurrency ?: expense.baseCurrency,
            exchangeRate = expense.exchangeRate,
        )
    }

    private fun getCostAsExpenseCreator(expense: Expense): BigDecimal {
        return expense.expenseParticipants
            .filter { participant -> participant.participantId != expense.creatorId }
            .fold(BigDecimal.ZERO) { acc, obj -> acc + obj.participantCost }
    }
}
