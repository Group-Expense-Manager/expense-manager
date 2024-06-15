package pl.edu.agh.gem.external.dto.expense

import jakarta.validation.constraints.NotBlank
import pl.edu.agh.gem.annotation.nullorblank.NullOrNotBlank
import pl.edu.agh.gem.internal.model.expense.Decision
import pl.edu.agh.gem.internal.model.expense.ExpenseDecision
import pl.edu.agh.gem.internal.validation.ValidationMessage.EXPENSE_ID_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.GROUP_ID_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.MESSAGE_NULL_OR_NOT_BLANK

data class ExpenseDecisionRequest(
    @field:NotBlank(message = EXPENSE_ID_NOT_BLANK)
    val expenseId: String,
    @field:NotBlank(message = GROUP_ID_NOT_BLANK)
    val groupId: String,
    val decision: Decision,
    @field:NullOrNotBlank(message = MESSAGE_NULL_OR_NOT_BLANK)
    val message: String?,
) {
    fun toDomain(userId: String) = ExpenseDecision(
        userId = userId,
        expenseId = expenseId,
        groupId = groupId,
        decision = decision,
        message = message,
    )
}
