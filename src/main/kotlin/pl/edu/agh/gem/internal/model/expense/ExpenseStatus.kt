package pl.edu.agh.gem.internal.model.expense

enum class ExpenseStatus {
    ACCEPTED,
    REJECTED,
    PENDING,
    ;

    fun changedToAccepted(currentStatus: ExpenseStatus): Boolean {
        return this != ACCEPTED && currentStatus == ACCEPTED
    }

    fun changedFromAccepted(previousStatus: ExpenseStatus): Boolean {
        return previousStatus == ACCEPTED && this != ACCEPTED
    }

    companion object {
        fun reduce(statuses: List<ExpenseStatus>): ExpenseStatus {
            return when {
                statuses.contains(REJECTED) -> REJECTED
                statuses.all { it == ACCEPTED } -> ACCEPTED
                else -> PENDING
            }
        }
    }
}
