package pl.edu.agh.gem.internal.validation

import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_AVAILABLE
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.internal.validation.ValidationMessage.TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class CurrenciesValidator : BaseValidator<ExpenseDataWrapper>() {
    override val checks: List<Check<ExpenseDataWrapper>> = listOf(
        Check(BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES) { this.validateBaseCurrencyInGroupCurrencies(it) },
        Check(BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY) { this.validateBaseCurrencyNotEqualTargetCurrency(it) },
        Check(TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES) { this.validateTargetCurrencyInGroupCurrencies(it) },
        Check(BASE_CURRENCY_NOT_AVAILABLE) { this.validateBaseCurrencyAvailable(it) },
    )

    private fun validateBaseCurrencyInGroupCurrencies(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        return expenseDataWrapper.expense.targetCurrency != null ||
            expenseDataWrapper.groupCurrencies.currencies.any { it.code == expenseDataWrapper.expense.baseCurrency }
    }

    private fun validateBaseCurrencyNotEqualTargetCurrency(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        return expenseDataWrapper.expense.targetCurrency == null ||
            expenseDataWrapper.expense.baseCurrency != expenseDataWrapper.expense.targetCurrency
    }

    private fun validateTargetCurrencyInGroupCurrencies(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        return expenseDataWrapper.expense.targetCurrency == null ||
            expenseDataWrapper.groupCurrencies.currencies.any { it.code == expenseDataWrapper.expense.targetCurrency }
    }

    private fun validateBaseCurrencyAvailable(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        return expenseDataWrapper.expense.targetCurrency == null ||
            expenseDataWrapper.availableCurrencies.currencies.any { it.code == expenseDataWrapper.expense.baseCurrency }
    }
}
