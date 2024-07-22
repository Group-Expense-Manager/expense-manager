package pl.edu.agh.gem.internal.validation.creation
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.group.Currencies
import pl.edu.agh.gem.internal.model.group.GroupData
import pl.edu.agh.gem.validator.DataWrapper

data class ExpenseCreationDataWrapper(
    val groupData: GroupData,
    val expense: Expense,
    val availableCurrencies: Currencies,
) : DataWrapper
