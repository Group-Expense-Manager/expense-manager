package pl.edu.agh.gem.external.dto.expense

import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipant
import pl.edu.agh.gem.internal.model.expense.FxData
import java.math.BigDecimal
import java.time.Instant

data class AcceptedGroupExpensesResponse(
    val groupId: String,
    val expenses: List<AcceptedGroupExpenseDto>,
)

data class AcceptedGroupExpenseDto(
    val creatorId: String,
    val title: String,
    val amount: AmountDto,
    val fxData: FxDataDto?,
    val participants: List<AcceptedGroupExpenseParticipantDto>,
    val expenseDate: Instant,
)

fun Expense.toAcceptedGroupExpenseDto() =
    AcceptedGroupExpenseDto(
        creatorId = creatorId,
        title = title,
        amount = amount.toAmountDto(),
        fxData = fxData?.toDto(),
        participants = expenseParticipants.map { it.toAcceptedGroupExpenseParticipantDto() },
        expenseDate = expenseDate,
    )

data class AcceptedGroupExpenseParticipantDto(
    val participantId: String,
    val participantCost: BigDecimal,
)

private fun ExpenseParticipant.toAcceptedGroupExpenseParticipantDto() =
    AcceptedGroupExpenseParticipantDto(
        participantId = participantId,
        participantCost = participantCost,
    )

fun List<Expense>.toAcceptedGroupExpensesResponse(groupId: String) =
    AcceptedGroupExpensesResponse(
        groupId = groupId,
        expenses = map { it.toAcceptedGroupExpenseDto() },
    )

data class FxDataDto(
    val targetCurrency: String,
    val exchangeRate: BigDecimal,
)

fun FxData.toDto() =
    FxDataDto(
        targetCurrency = targetCurrency,
        exchangeRate = exchangeRate,
    )
