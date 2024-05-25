package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.client.CurrencyManagerClient
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.group.Group
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.internal.validation.CostValidator
import pl.edu.agh.gem.internal.validation.CurrenciesValidator
import pl.edu.agh.gem.internal.validation.ExpenseDataWrapper
import pl.edu.agh.gem.internal.validation.ParticipantValidator
import pl.edu.agh.gem.model.GroupMembers
import pl.edu.agh.gem.validator.ValidatorList.Companion.validatorsOf
import pl.edu.agh.gem.validator.ValidatorsException
import java.time.Instant

@Service
class ExpenseService(
    private val groupManagerClient: GroupManagerClient,
    private val currencyManagerClient: CurrencyManagerClient,
    private val expenseRepository: ExpenseRepository,
) {
    private val expenseValidators = validatorsOf(
        CostValidator(),
        ParticipantValidator(),
        CurrenciesValidator(),
    )

    fun getMembers(groupId: String): GroupMembers {
        return groupManagerClient.getMembers(groupId)
    }

    fun getGroup(groupId: String): Group {
        return groupManagerClient.getGroup(groupId)
    }

    fun create(group: Group, expense: Expense): Expense {
        expenseValidators
            .getFailedValidations(createExpenseDataWrapper(group, expense))
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }
        return expenseRepository.create(
            expense.copy(
                exchangeRate = getExchangeRate(expense.baseCurrency, expense.targetCurrency, expense.expenseDate),
            ),
        )
    }

    private fun getExchangeRate(baseCurrency: String, targetCurrency: String?, date: Instant) =
        targetCurrency?.let { currencyManagerClient.getExchangeRate(baseCurrency, targetCurrency, date) }

    private fun createExpenseDataWrapper(group: Group, expense: Expense): ExpenseDataWrapper {
        return ExpenseDataWrapper(
            group,
            expense,
            currencyManagerClient.getAvailableCurrencies(),
        )
    }

    fun getExpense(expenseId: String, groupId: String): Expense {
        return expenseRepository.findByExpenseIdAndGroupId(expenseId, groupId) ?: throw MissingExpenseException(expenseId, groupId)
    }

    fun getGroupExpenses(groupId: String): List<Expense> {
        return expenseRepository.findByGroupId(groupId)
    }
}

class MissingExpenseException(expenseId: String, groupId: String) :
    RuntimeException("Failed to find expense with id: $expenseId and groupId: $groupId")
