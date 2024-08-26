package pl.edu.agh.gem.internal.validation.currency

import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_AVAILABLE
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.internal.validation.ValidationMessage.TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES
import pl.edu.agh.gem.validator.BaseValidator
import pl.edu.agh.gem.validator.Check

class CurrenciesValidator : BaseValidator<CurrencyDataWrapper>() {
    override val checks: List<Check<CurrencyDataWrapper>> = listOf(
        Check(BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES) { validateBaseCurrencyInGroupCurrencies(it) },
        Check(BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY) { validateBaseCurrencyNotEqualTargetCurrency(it) },
        Check(TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES) { validateTargetCurrencyInGroupCurrencies(it) },
        Check(BASE_CURRENCY_NOT_AVAILABLE) { validateBaseCurrencyAvailable(it) },
    )

    private fun validateBaseCurrencyInGroupCurrencies(currencyDataWrapper: CurrencyDataWrapper): Boolean {
        return currencyDataWrapper.currencyData.targetCurrency != null ||
            currencyDataWrapper.currencyData.groupCurrencies.any { it.code == currencyDataWrapper.currencyData.baseCurrency }
    }

    private fun validateBaseCurrencyNotEqualTargetCurrency(currencyDataWrapper: CurrencyDataWrapper): Boolean {
        return currencyDataWrapper.currencyData.targetCurrency == null ||
            currencyDataWrapper.currencyData.baseCurrency != currencyDataWrapper.currencyData.targetCurrency
    }

    private fun validateTargetCurrencyInGroupCurrencies(currencyDataWrapper: CurrencyDataWrapper): Boolean {
        return currencyDataWrapper.currencyData.targetCurrency == null ||
            currencyDataWrapper.currencyData.groupCurrencies.any { it.code == currencyDataWrapper.currencyData.targetCurrency }
    }

    private fun validateBaseCurrencyAvailable(currencyDataWrapper: CurrencyDataWrapper): Boolean {
        return currencyDataWrapper.currencyData.targetCurrency == null ||
            currencyDataWrapper.currencyData.availableCurrencies.any { it.code == currencyDataWrapper.currencyData.baseCurrency }
    }
}
