package pl.edu.agh.gem.external.controller

import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import pl.edu.agh.gem.error.SimpleError
import pl.edu.agh.gem.error.SimpleErrorsHolder
import pl.edu.agh.gem.error.handleError
import pl.edu.agh.gem.error.handleNotValidException
import pl.edu.agh.gem.error.withCode
import pl.edu.agh.gem.error.withDetails
import pl.edu.agh.gem.error.withMessage
import pl.edu.agh.gem.exception.UserWithoutGroupAccessException
import pl.edu.agh.gem.internal.client.CurrencyManagerClientException
import pl.edu.agh.gem.internal.client.GroupManagerClientException
import pl.edu.agh.gem.internal.client.RetryableCurrencyManagerClientException
import pl.edu.agh.gem.internal.client.RetryableGroupManagerClientException
import pl.edu.agh.gem.internal.service.ExpenseDeletionAccessException
import pl.edu.agh.gem.internal.service.ExpenseUpdateAccessException
import pl.edu.agh.gem.internal.service.MissingExpenseException
import pl.edu.agh.gem.internal.service.NoExpenseUpdateException
import pl.edu.agh.gem.internal.service.UserNotParticipantException
import pl.edu.agh.gem.validator.ValidatorsException

@ControllerAdvice
@Order(LOWEST_PRECEDENCE)
class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        exception: MethodArgumentNotValidException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleNotValidException(exception), BAD_REQUEST)
    }

    @ExceptionHandler(UserWithoutGroupAccessException::class)
    fun handleUserWithoutGroupAccessException(
        exception: UserWithoutGroupAccessException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), FORBIDDEN)
    }

    @ExceptionHandler(ExpenseDeletionAccessException::class)
    fun handleExpenseDeletionAccessException(
        exception: ExpenseDeletionAccessException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), FORBIDDEN)
    }

    @ExceptionHandler(ExpenseUpdateAccessException::class)
    fun handleExpenseUpdateAccessException(
        exception: ExpenseUpdateAccessException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), FORBIDDEN)
    }

    @ExceptionHandler(NoExpenseUpdateException::class)
    fun handleNoExpenseUpdateException(
        exception: NoExpenseUpdateException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), BAD_REQUEST)
    }

    @ExceptionHandler(ValidatorsException::class)
    fun handleValidatorsException(exception: ValidatorsException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleValidatorException(exception), BAD_REQUEST)
    }

    @ExceptionHandler(MissingExpenseException::class)
    fun handleMissingExpenseException(exception: MissingExpenseException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), NOT_FOUND)
    }

    @ExceptionHandler(UserNotParticipantException::class)
    fun handleUserNotParticipantException(exception: UserNotParticipantException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), FORBIDDEN)
    }

    @ExceptionHandler(CurrencyManagerClientException::class)
    fun handleCurrencyManagerClientException(exception: CurrencyManagerClientException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(RetryableCurrencyManagerClientException::class)
    fun handleRetryableCurrencyManagerClientException(
        exception: RetryableCurrencyManagerClientException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(GroupManagerClientException::class)
    fun handleGroupManagerClientException(exception: GroupManagerClientException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(RetryableGroupManagerClientException::class)
    fun handleRetryableGroupManagerClientException(
        exception: RetryableGroupManagerClientException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), INTERNAL_SERVER_ERROR)
    }

    private fun handleValidatorException(exception: ValidatorsException): SimpleErrorsHolder {
        val errors = exception.failedValidations
            .map { error ->
                SimpleError()
                    .withCode("VALIDATOR_ERROR")
                    .withDetails(error)
                    .withMessage(error)
            }
        return SimpleErrorsHolder(errors)
    }
}
