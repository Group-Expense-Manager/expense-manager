package pl.edu.agh.gem.external.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.exception.UserWithoutGroupAccessException
import pl.edu.agh.gem.external.dto.expense.ExpenseCreationRequest
import pl.edu.agh.gem.external.dto.expense.ExpenseCreationResponse
import pl.edu.agh.gem.internal.service.ExpenseService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.model.GroupMembers
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import pl.edu.agh.gem.security.GemUserId

@RestController
class ExpenseController(
    private val expenseService: ExpenseService,
) {
    @PostMapping("$EXTERNAL/expenses", consumes = [APPLICATION_JSON_INTERNAL_VER_1], produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(CREATED)
    fun createExpense(
        @GemUserId userId: String,
        @RequestParam groupId: String,
        @Valid @RequestBody
        expenseCreationRequest: ExpenseCreationRequest,
    ): ExpenseCreationResponse {
        val groupMembers = expenseService.getGroupMembers(groupId)

        userId.checkIfUserHaveAccess(groupMembers)

        return ExpenseCreationResponse(
            expenseService.create(groupMembers, expenseCreationRequest.toDomain(userId, groupId)).id,
        )
    }

    private fun String.checkIfUserHaveAccess(groupMembers: GroupMembers) {
        groupMembers.members.find { it.id == this } ?: throw UserWithoutGroupAccessException(this)
    }
}
