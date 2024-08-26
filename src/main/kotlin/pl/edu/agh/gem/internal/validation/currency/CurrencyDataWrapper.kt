package pl.edu.agh.gem.internal.validation.currency

import pl.edu.agh.gem.validator.DataWrapper

interface CurrencyDataWrapper : DataWrapper {
    val currencyData: CurrencyData
}
