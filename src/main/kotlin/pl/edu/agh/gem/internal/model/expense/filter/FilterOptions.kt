package pl.edu.agh.gem.internal.model.expense.filter

import pl.edu.agh.gem.internal.model.expense.ExpenseStatus

data class FilterOptions(
    val title: String?,
    val status: ExpenseStatus?,
    val creatorId: String?,
    val currency: String?,
    val sortedBy: SortedBy,
    val sortOrder: SortOrder,
)
