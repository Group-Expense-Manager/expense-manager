package pl.edu.agh.gem.external.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.exception.UserWithoutGroupAccessException
import pl.edu.agh.gem.external.dto.expense.ExpenseCreationRequest
import pl.edu.agh.gem.external.dto.expense.ExpenseCreationResponse
import pl.edu.agh.gem.external.dto.expense.ExpenseDecisionRequest
import pl.edu.agh.gem.external.dto.expense.ExpenseResponse
import pl.edu.agh.gem.external.dto.expense.ExpenseUpdateRequest
import pl.edu.agh.gem.external.dto.expense.ExpenseUpdateResponse
import pl.edu.agh.gem.external.dto.expense.ExternalGroupExpensesResponse
import pl.edu.agh.gem.external.dto.expense.toExpenseUpdateResponse
import pl.edu.agh.gem.external.dto.expense.toExternalGroupExpensesResponse
import pl.edu.agh.gem.internal.service.ExpenseService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.model.GroupMembers
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import pl.edu.agh.gem.security.GemUserId

@RestController
@RequestMapping("$EXTERNAL/expenses")
class ExternalExpenseController(
    private val expenseService: ExpenseService,
) {
    @PostMapping(consumes = [APPLICATION_JSON_INTERNAL_VER_1], produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(CREATED)
    fun createExpense(
        @GemUserId userId: String,
        @RequestParam groupId: String,
        @Valid @RequestBody
        expenseCreationRequest: ExpenseCreationRequest,
    ): ExpenseCreationResponse {
        val group = expenseService.getGroup(groupId)
        userId.checkIfUserHaveAccess(group.members)

        return ExpenseCreationResponse(
            expenseService.create(group, expenseCreationRequest.toDomain(userId, groupId)).id,
        )
    }

    @GetMapping("{expenseId}/groups/{groupId}", produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun getExpense(
        @GemUserId userId: String,
        @PathVariable expenseId: String,
        @PathVariable groupId: String,
    ): ExpenseResponse {
        userId.checkIfUserHaveAccess(expenseService.getMembers(groupId))
        return ExpenseResponse.fromExpense(expenseService.getExpense(expenseId, groupId))
    }

    @GetMapping(produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun getExternalGroupExpenses(
        @GemUserId userId: String,
        @RequestParam groupId: String,
    ): ExternalGroupExpensesResponse {
        userId.checkIfUserHaveAccess(expenseService.getMembers(groupId))
        return expenseService.getExternalGroupExpenses(groupId).toExternalGroupExpensesResponse()
    }

    @PostMapping("decide", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun decide(
        @GemUserId userId: String,
        @Valid @RequestBody
        expenseDecisionRequest: ExpenseDecisionRequest,
    ) {
        userId.checkIfUserHaveAccess(expenseService.getMembers(expenseDecisionRequest.groupId))
        expenseService.decide(expenseDecisionRequest.toDomain(userId))
    }

    @DeleteMapping("{expenseId}/groups/{groupId}")
    @ResponseStatus(OK)
    fun deleteExpense(
        @GemUserId userId: String,
        @PathVariable expenseId: String,
        @PathVariable groupId: String,
    ) {
        userId.checkIfUserHaveAccess(expenseService.getMembers(groupId))
        expenseService.deleteExpense(expenseId, groupId, userId)
    }

    @PutMapping("{expenseId}/groups/{groupId}", consumes = [APPLICATION_JSON_INTERNAL_VER_1], produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun updateExpense(
        @GemUserId userId: String,
        @PathVariable expenseId: String,
        @PathVariable groupId: String,
        @Valid @RequestBody
        expenseUpdateRequest: ExpenseUpdateRequest,
    ): ExpenseUpdateResponse {
        val group = expenseService.getGroup(groupId)
        userId.checkIfUserHaveAccess(group.members)
        return expenseService.updateExpense(group, expenseUpdateRequest.toDomain(expenseId, groupId, userId)).toExpenseUpdateResponse()
    }

    private fun String.checkIfUserHaveAccess(groupMembers: GroupMembers) {
        groupMembers.members.find { it.id == this } ?: throw UserWithoutGroupAccessException(this)
    }
}
