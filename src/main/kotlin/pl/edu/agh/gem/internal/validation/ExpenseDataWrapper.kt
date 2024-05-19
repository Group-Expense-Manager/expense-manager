package pl.edu.agh.gem.internal.validation

import pl.edu.agh.gem.internal.model.currency.Currencies
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.model.GroupMembers
import pl.edu.agh.gem.validator.DataWrapper

data class ExpenseDataWrapper(
    val groupMembers: GroupMembers,
    val expense: Expense,
    val groupCurrencies: Currencies,
    val availableCurrencies: Currencies,
) : DataWrapper
