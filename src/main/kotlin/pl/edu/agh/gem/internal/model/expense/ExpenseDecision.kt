package pl.edu.agh.gem.internal.model.expense

data class ExpenseDecision(
    val userId: String,
    val expenseId: String,
    val groupId: String,
    val decision: Decision,
    val message: String?,
)
