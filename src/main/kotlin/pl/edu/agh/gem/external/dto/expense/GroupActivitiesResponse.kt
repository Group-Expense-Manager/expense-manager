package pl.edu.agh.gem.external.dto.expense

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import java.time.Instant

data class GroupActivitiesResponse(
    val groupId: String,
    val expenses: List<GroupActivityDto>,
)

data class GroupActivityDto(
    val expenseId: String,
    val creatorId: String,
    val title: String,
    val amount: AmountDto,
    val fxData: FxDataDto?,
    val status: ExpenseStatus,
    val participantIds: List<String>,
    val expenseDate: Instant,
) {
    companion object {
        fun fromExpense(expense: Expense) =
            GroupActivityDto(
                expenseId = expense.id,
                creatorId = expense.creatorId,
                title = expense.title,
                amount = expense.amount.toAmountDto(),
                fxData = expense.fxData?.toDto(),
                status = expense.status,
                participantIds = expense.expenseParticipants.map { it.participantId },
                expenseDate = expense.expenseDate,
            )
    }
}

fun List<Expense>.toGroupActivitiesResponse(groupId: String) =
    GroupActivitiesResponse(
        groupId = groupId,
        expenses = map { GroupActivityDto.fromExpense(it) },
    )
