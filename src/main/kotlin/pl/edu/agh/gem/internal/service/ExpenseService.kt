package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.client.CurrencyManagerClient
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.model.currency.ExchangeRate
import pl.edu.agh.gem.internal.model.expense.Expense
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

    fun getGroupMembers(groupId: String): GroupMembers {
        return groupManagerClient.getGroupMembers(groupId)
    }

    fun create(groupMembers: GroupMembers, expense: Expense): Expense {
        expenseValidators
            .getFailedValidations(createExpenseDataWrapper(groupMembers, expense))
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }
        val exchangeRate = getExchangeRate(expense.baseCurrency, expense.targetCurrency, expense.expenseDate)
        return expenseRepository.create(expense.copy(exchangeRate = exchangeRate))
    }

    private fun getExchangeRate(baseCurrency: String, targetCurrency: String?, date: Instant): ExchangeRate? {
        targetCurrency?.let { return currencyManagerClient.getExchangeRate(baseCurrency, targetCurrency, date) } ?: return null
    }

    private fun createExpenseDataWrapper(groupMembers: GroupMembers, expense: Expense): ExpenseDataWrapper {
        return ExpenseDataWrapper(
            groupMembers,
            expense,
            groupManagerClient.getGroupOptions(expense.groupId).currencies,
            currencyManagerClient.getAvailableCurrencies(),
        )
    }
}
