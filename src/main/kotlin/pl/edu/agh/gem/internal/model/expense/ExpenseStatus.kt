package pl.edu.agh.gem.internal.model.expense

enum class ExpenseStatus {
    ACCEPTED,
    REJECTED,
    PENDING,
    ;

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
