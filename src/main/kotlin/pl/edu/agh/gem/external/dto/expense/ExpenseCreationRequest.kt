package pl.edu.agh.gem.external.dto.expense

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME
import pl.edu.agh.gem.annotation.decimalplaces.DecimalPlaces
import pl.edu.agh.gem.annotation.nullorblank.NullOrNotBlank
import pl.edu.agh.gem.annotation.nullorpattern.NullOrPattern
import pl.edu.agh.gem.internal.model.expense.ExpenseCreation
import pl.edu.agh.gem.internal.model.expense.ExpenseParticipant
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.validation.ValidationMessage.ATTACHMENT_ID_NULL_OR_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.BASE_CURRENCY_PATTERN
import pl.edu.agh.gem.internal.validation.ValidationMessage.EXPENSE_PARTICIPANTS_NOT_EMPTY
import pl.edu.agh.gem.internal.validation.ValidationMessage.MAX_TOTAL_COST
import pl.edu.agh.gem.internal.validation.ValidationMessage.MESSAGE_NULL_OR_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.PARTICIPANT_ID_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.POSITIVE_PARTICIPANT_COST
import pl.edu.agh.gem.internal.validation.ValidationMessage.POSITIVE_TOTAL_COST
import pl.edu.agh.gem.internal.validation.ValidationMessage.TARGET_CURRENCY_PATTERN
import pl.edu.agh.gem.internal.validation.ValidationMessage.TITLE_MAX_LENGTH
import pl.edu.agh.gem.internal.validation.ValidationMessage.TITLE_NOT_BLANK
import pl.edu.agh.gem.internal.validation.ValidationMessage.TOTAL_COST_DECIMAL_PLACES
import java.math.BigDecimal
import java.time.Instant

data class ExpenseCreationRequest(
    @field:NotBlank(message = TITLE_NOT_BLANK)
    @field:Size(max = 30, message = TITLE_MAX_LENGTH)
    val title: String,
    @field:Positive(message = POSITIVE_TOTAL_COST)
    @field:DecimalMax(value = "100000", inclusive = false, message = MAX_TOTAL_COST)
    @field:DecimalPlaces(max = 2, message = TOTAL_COST_DECIMAL_PLACES)
    val totalCost: BigDecimal,
    @field:NotBlank(message = BASE_CURRENCY_NOT_BLANK)
    @field:Pattern(regexp = "[A-Z]{3}", message = BASE_CURRENCY_PATTERN)
    val baseCurrency: String,
    @field:NullOrPattern(message = TARGET_CURRENCY_PATTERN, pattern = "[A-Z]{3}")
    val targetCurrency: String?,
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
    fun toDomain(userId: String, groupId: String) =
        ExpenseCreation(
            groupId = groupId,
            creatorId = userId,
            title = title,
            totalCost = totalCost,
            baseCurrency = baseCurrency,
            targetCurrency = targetCurrency,
            expenseDate = expenseDate,
            message = message,
            attachmentId = attachmentId,
            expenseParticipantsCost = expenseParticipants.map { it.toExpenseParticipantCost() },
        )
}

data class ExpenseParticipantRequestData(
    @field:NotBlank(message = PARTICIPANT_ID_NOT_BLANK)
    val participantId: String,
    @field:Positive(message = POSITIVE_PARTICIPANT_COST)
    val participantCost: BigDecimal,
) {
    fun toDomain(creatorId: String) =
        ExpenseParticipant(
            participantId = participantId,
            participantCost = participantCost,
            participantStatus = if (creatorId == participantId) ACCEPTED else PENDING,
        )
}
