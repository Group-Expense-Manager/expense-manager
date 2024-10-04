package pl.edu.agh.gem.internal.validation

object ValidationMessage {
    const val TITLE_NOT_BLANK = "Title can not be blank"
    const val TITLE_MAX_LENGTH = "Name must not exceed 30 characters"
    const val POSITIVE_AMOUNT = "Amount value must be positive"
    const val MAX_AMOUNT = "Amount value must be less than 100000"
    const val AMOUNT_DECIMAL_PLACES = "Amount value can have a maximum of two decimal places"
    const val BASE_CURRENCY_NOT_BLANK = "Base currency can not be blank"
    const val BASE_CURRENCY_PATTERN = "Base currency must be a 3-letter uppercase code"
    const val TARGET_CURRENCY_PATTERN = "Target Currency must be null or a 3-letter uppercase code"
    const val EXPENSE_PARTICIPANTS_NOT_EMPTY = "Expense participant list can not be empty"
    const val ATTACHMENT_ID_NULL_OR_NOT_BLANK = "AttachmentId must be null or not blank"
    const val PARTICIPANT_ID_NOT_BLANK = "Participant's id can not be blank"
    const val POSITIVE_PARTICIPANT_COST = "Participant's cost must be positive"

    const val PARTICIPANT_COSTS_HIGHER_THAN_TOTAL_COST = "Participants costs are higher than full cost"
    const val CREATOR_IN_PARTICIPANTS = "Creator can't be an participant"
    const val USER_NOT_PARTICIPANT = "User is not participant"

    const val DUPLICATED_PARTICIPANT = "One of participants is duplicated"
    const val PARTICIPANT_NOT_GROUP_MEMBER = "All participants must be group members"
    const val BASE_CURRENCY_NOT_IN_GROUP_CURRENCIES = "Base currency must be in a group currencies"
    const val BASE_CURRENCY_EQUAL_TO_TARGET_CURRENCY = "Base currency must be different than target currency"
    const val TARGET_CURRENCY_NOT_IN_GROUP_CURRENCIES = "Target currency must be in a group currencies"
    const val BASE_CURRENCY_NOT_AVAILABLE = "Base currency is not available"

    const val EXPENSE_ID_NOT_BLANK = "Expense id can not be blank"
    const val GROUP_ID_NOT_BLANK = "Group id can not be blank"
    const val MESSAGE_NULL_OR_NOT_BLANK = "Message can not be blank and not null at the same time"

    const val USER_NOT_CREATOR = "Only creator can update payment"
}
