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
import pl.edu.agh.gem.external.dto.expense.ExpenseDecisionRequest
import pl.edu.agh.gem.external.dto.expense.ExpenseResponse
import pl.edu.agh.gem.external.dto.expense.ExpenseUpdateRequest
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.service.ExpenseService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.model.GroupMembers
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import pl.edu.agh.gem.security.GemUserId

@RestController
@RequestMapping("$EXTERNAL/expenses")
class ExternalExpenseController(
    private val expenseService: ExpenseService,
    private val groupManagerClient: GroupManagerClient,
) {
    @PostMapping(consumes = [APPLICATION_JSON_INTERNAL_VER_1], produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(CREATED)
    fun createExpense(
        @GemUserId userId: String,
        @RequestParam groupId: String,
        @Valid @RequestBody
        expenseCreationRequest: ExpenseCreationRequest,
    ): ExpenseResponse {
        val group = expenseService.getGroup(groupId)
        userId.checkIfUserHaveAccess(group.members)

        return ExpenseResponse.fromExpense(
            expenseService.create(group, expenseCreationRequest.toDomain(userId, groupId)),
        )
    }

    @GetMapping("{expenseId}/groups/{groupId}", produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun getExpense(
        @GemUserId userId: String,
        @PathVariable expenseId: String,
        @PathVariable groupId: String,
    ): ExpenseResponse {
        userId.checkIfUserHaveAccess(groupId)
        return ExpenseResponse.fromExpense(expenseService.getExpense(expenseId, groupId))
    }

    @PostMapping("decide", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun decide(
        @GemUserId userId: String,
        @Valid @RequestBody
        expenseDecisionRequest: ExpenseDecisionRequest,
    ) {
        userId.checkIfUserHaveAccess(expenseDecisionRequest.groupId)
        expenseService.decide(expenseDecisionRequest.toDomain(userId))
    }

    @DeleteMapping("{expenseId}/groups/{groupId}")
    @ResponseStatus(OK)
    fun deleteExpense(
        @GemUserId userId: String,
        @PathVariable expenseId: String,
        @PathVariable groupId: String,
    ) {
        userId.checkIfUserHaveAccess(groupId)
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
    ): ExpenseResponse {
        val group = expenseService.getGroup(groupId)
        userId.checkIfUserHaveAccess(group.members)
        return ExpenseResponse.fromExpense(expenseService.updateExpense(group, expenseUpdateRequest.toDomain(expenseId, groupId, userId)))
    }

    private fun String.checkIfUserHaveAccess(groupMembers: GroupMembers) {
        groupMembers.members.find { it.id == this } ?: throw UserWithoutGroupAccessException(this)
    }

    private fun String.checkIfUserHaveAccess(groupId: String) =
        groupManagerClient.getUserGroups(this).find { it.groupId == groupId } ?: throw UserWithoutGroupAccessException(this)
}
