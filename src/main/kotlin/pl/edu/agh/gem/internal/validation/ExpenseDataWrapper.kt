package pl.edu.agh.gem.internal.validation

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.group.Currencies
import pl.edu.agh.gem.internal.model.group.Group
import pl.edu.agh.gem.validator.DataWrapper

data class ExpenseDataWrapper(
    val group: Group,
    val expense: Expense,
    val availableCurrencies: Currencies,
) : DataWrapper
