package pl.edu.agh.gem.internal.validation

import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_AVAILABLE
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.internal.validation.ValidationMessage.TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class CurrenciesValidator : BaseValidator<ExpenseDataWrapper>() {
    override val checks: List<Check<ExpenseDataWrapper>> = listOf(
        Check(BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES) { validateBaseCurrencyInGroupCurrencies(it) },
        Check(BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY) { validateBaseCurrencyNotEqualTargetCurrency(it) },
        Check(TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES) { validateTargetCurrencyInGroupCurrencies(it) },
        Check(BASE_CURRENCY_NOT_AVAILABLE) { validateBaseCurrencyAvailable(it) },
    )

    private fun validateBaseCurrencyInGroupCurrencies(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        return expenseDataWrapper.hasTargetCurrency() || expenseDataWrapper.groupCurrenciesContains(expenseDataWrapper.expense.baseCurrency)
    }

    private fun validateBaseCurrencyNotEqualTargetCurrency(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        return !expenseDataWrapper.hasTargetCurrency() || expenseDataWrapper.expense.baseCurrency != expenseDataWrapper.expense.targetCurrency
    }

    private fun validateTargetCurrencyInGroupCurrencies(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        return !expenseDataWrapper.hasTargetCurrency() || expenseDataWrapper.groupCurrenciesContains(expenseDataWrapper.expense.targetCurrency)
    }

    private fun validateBaseCurrencyAvailable(expenseDataWrapper: ExpenseDataWrapper): Boolean {
        return !expenseDataWrapper.hasTargetCurrency() || expenseDataWrapper.availableCurrenciesContains(expenseDataWrapper.expense.baseCurrency)
    }

    private fun ExpenseDataWrapper.hasTargetCurrency() = expense.targetCurrency != null
    private fun ExpenseDataWrapper.groupCurrenciesContains(
        currency: String?,
    ) = group.currencies.any { it.code == currency }
    private fun ExpenseDataWrapper.availableCurrenciesContains(
        currency: String?,
    ) = availableCurrencies.any { it.code == currency }
}
