package pl.edu.agh.gem.internal.model.expense

enum class Decision {
    ACCEPT,
    REJECT,
    ;

    fun toExpenseStatus(): ExpenseStatus {
        return when (this) {
            ACCEPT -> ExpenseStatus.ACCEPTED
            REJECT -> ExpenseStatus.REJECTED
        }
    }

    fun toExpenseAction(): ExpenseAction {
        return when (this) {
            ACCEPT -> ExpenseAction.ACCEPTED
            REJECT -> ExpenseAction.REJECTED
        }
    }
}
