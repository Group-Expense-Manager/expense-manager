package pl.edu.agh.gem.internal.validation

object ValidationMessage {
    const val TITLE_NOT_BLANK = "Title can not be blank"
    const val TITLE_MAX_LENGTH = "Name must not exceed 30 characters"
    const val POSITIVE_COST = "Cost must be positive"
    const val BASE_CURRENCY_NOT_BLANK = "Base currency can not be blank"
    const val BASE_CURRENCY_PATTERN = "Base currency must be a 3-letter uppercase code"
    const val TARGET_CURRENCY_PATTERN = "Target Currency must be null or a 3-letter uppercase code"
    const val EXPENSE_PARTICIPANTS_NOT_EMPTY = "Expense participant list can not be empty"
    const val ATTACHMENT_ID_NULL_OR_NOT_BLANK = "AttachmentId must be null or not blank"
    const val PARTICIPANT_ID_NOT_BLANK = "Participant's id can not be blank"
    const val POSITIVE_PARTICIPANT_COST = "Participant's cost must be positive"

    const val COST_NOT_SUM_UP = "Participants costs do not sum up to full cost"
    const val USER_NOT_PARTICIPANT = "User is not participant"
    const val DUPLICATED_PARTICIPANT = "One of participants is duplicated"
    const val PARTICIPANT_MIN_SIZE = "Number of participants must be above 1"
    const val PARTICIPANT_NOT_GROUP_MEMBER = "All participants must be group members"
    const val BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES = "Base currency must be in a group currencies"
    const val BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY = "Base currency must be different than target currency"
    const val TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES = "Target currency must be in a group currencies"
    const val BASE_CURRENCY_NOT_AVAILABLE = "Base currency is not available"

    const val EXPENSE_ID_NOT_BLANK = "Expense id can not be blank"
    const val GROUP_ID_NOT_BLANK = "Group id can not be blank"
    const val MESSAGE_NULL_OR_NOT_BLANK = "Message can not be blank and not null at the same time"

    const val CREATOR_DECISION = "Expense creator can not accept or reject expense"

    const val NO_MODIFICATION = "Update does not change anything"
    const val USER_NOT_CREATOR = "Only creator can update payment"
}
