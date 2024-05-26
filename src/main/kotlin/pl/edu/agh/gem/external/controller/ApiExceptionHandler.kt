package pl.edu.agh.gem.external.controller

import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
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
import pl.edu.agh.gem.internal.service.GroupWithoutExpenseException
import pl.edu.agh.gem.internal.service.MissingExpenseException
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

    @ExceptionHandler(ValidatorsException::class)
    fun handleValidatorsException(exception: ValidatorsException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleValidatorException(exception), BAD_REQUEST)
    }

    @ExceptionHandler(MissingExpenseException::class)
    fun handleMissingExpenseException(exception: MissingExpenseException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), NOT_FOUND)
    }

    @ExceptionHandler(GroupWithoutExpenseException::class)
    fun handleGroupWithoutExpenseException(exception: GroupWithoutExpenseException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), NOT_FOUND)
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
