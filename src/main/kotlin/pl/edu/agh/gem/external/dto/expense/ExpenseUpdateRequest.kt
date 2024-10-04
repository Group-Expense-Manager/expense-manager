package pl.edu.agh.gem.external.dto.expense

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME
import pl.edu.agh.gem.annotation.nullorblank.NullOrNotBlank
import pl.edu.agh.gem.annotation.nullorpattern.NullOrPattern
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipantCost
import pl.edu.agh.gem.internal.model.expense.ExpenseUpdate
import pl.edu.agh.gem.internal.validation.ValidationMessage.ATTACHMENT_ID_NULL_OR_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.EXPENSE_PARTICIPANTS_NOT_EMPTY
import pl.edu.agh.gem.internal.validation.ValidationMessage.MESSAGE_NULL_OR_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.TARGET_CURRENCY_PATTERN
import pl.edu.agh.gem.internal.validation.ValidationMessage.TITLE_MAX_LENGTH
import pl.edu.agh.gem.internal.validation.ValidationMessage.TITLE_NOT_BLANK
import java.time.Instant

data class ExpenseUpdateRequest(
    @field:NotBlank(message = TITLE_NOT_BLANK)
    @field:Size(max = 30, message = TITLE_MAX_LENGTH)
    val title: String,
    @field:Valid
    val amount: AmountDto,
    @field:NullOrPattern(message = TARGET_CURRENCY_PATTERN, pattern = "[A-Z]{3}")
    val targetCurrency: String? = null,
    @field:DateTimeFormat(iso = DATE_TIME)
    val expenseDate: Instant,
    @field:NotEmpty(message = EXPENSE_PARTICIPANTS_NOT_EMPTY)
    @field:Valid
    val expenseParticipants: List<ExpenseParticipantRequestData>,
    @field:NullOrNotBlank(message = MESSAGE_NULL_OR_NOT_BLANK)
    val message: String? = null,
    @field:NullOrNotBlank(message = ATTACHMENT_ID_NULL_OR_NOT_BLANK)
    val attachmentId: String?,
) {
    fun toDomain(expenseId: String, groupId: String, userId: String) =
        ExpenseUpdate(
            id = expenseId,
            groupId = groupId,
            userId = userId,
            title = title,
            amount = amount.toDomain(),
            targetCurrency = targetCurrency,
            expenseDate = expenseDate,
            expenseParticipantsCost = expenseParticipants.map { it.toExpenseParticipantCost() },
            message = message,
            attachmentId = attachmentId,
        )
}

fun ExpenseParticipantRequestData.toExpenseParticipantCost() = ExpenseParticipantCost(
    participantId = participantId,
    participantCost = participantCost,
)
