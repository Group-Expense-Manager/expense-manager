package pl.edu.agh.gem.internal.validation.creation

import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_AVAILABLE
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.internal.validation.ValidationMessage.TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class CurrenciesValidator : BaseValidator<ExpenseCreationDataWrapper>() {
    override val checks: List<Check<ExpenseCreationDataWrapper>> = listOf(
        Check(BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES) { validateBaseCurrencyInGroupCurrencies(it) },
        Check(BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY) { validateBaseCurrencyNotEqualTargetCurrency(it) },
        Check(TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES) { validateTargetCurrencyInGroupCurrencies(it) },
        Check(BASE_CURRENCY_NOT_AVAILABLE) { validateBaseCurrencyAvailable(it) },
    )

    private fun validateBaseCurrencyInGroupCurrencies(expenseCreationDataWrapper: ExpenseCreationDataWrapper): Boolean {
        return expenseCreationDataWrapper.expense.targetCurrency != null ||
            expenseCreationDataWrapper.groupData.currencies.any { it.code == expenseCreationDataWrapper.expense.baseCurrency }
    }

    private fun validateBaseCurrencyNotEqualTargetCurrency(expenseCreationDataWrapper: ExpenseCreationDataWrapper): Boolean {
        return expenseCreationDataWrapper.expense.targetCurrency == null ||
            expenseCreationDataWrapper.expense.baseCurrency != expenseCreationDataWrapper.expense.targetCurrency
    }

    private fun validateTargetCurrencyInGroupCurrencies(expenseCreationDataWrapper: ExpenseCreationDataWrapper): Boolean {
        return expenseCreationDataWrapper.expense.targetCurrency == null ||
            expenseCreationDataWrapper.groupData.currencies.any { it.code == expenseCreationDataWrapper.expense.targetCurrency }
    }

    private fun validateBaseCurrencyAvailable(expenseCreationDataWrapper: ExpenseCreationDataWrapper): Boolean {
        return expenseCreationDataWrapper.expense.targetCurrency == null ||
            expenseCreationDataWrapper.availableCurrencies.any { it.code == expenseCreationDataWrapper.expense.baseCurrency }
    }
}
