package pl.edu.agh.gem.external.controller

import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.external.dto.expense.GroupActivitiesResponse
import pl.edu.agh.gem.external.dto.expense.InternalGroupExpensesResponse
import pl.edu.agh.gem.external.dto.expense.UserExpensesResponse
import pl.edu.agh.gem.external.dto.expense.toGroupActivitiesResponse
import pl.edu.agh.gem.external.dto.expense.toInternalGroupExpensesResponse
import pl.edu.agh.gem.external.dto.expense.toUserExpensesResponse
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.filter.FilterOptions
import pl.edu.agh.gem.internal.model.expense.filter.SortOrder
import pl.edu.agh.gem.internal.model.expense.filter.SortedBy
import pl.edu.agh.gem.internal.service.ExpenseService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.paths.Paths.INTERNAL

@RestController
@RequestMapping("$INTERNAL/expenses")
class InternalExpenseController(
    private val expenseService: ExpenseService,
) {

    @GetMapping("groups/{groupId}/users/{userId}", produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun getUserExpenses(
        @PathVariable groupId: String,
        @PathVariable userId: String,
    ): UserExpensesResponse {
        return expenseService.getUserExpenses(groupId, userId).toUserExpensesResponse(userId)
    }

    @GetMapping("groups/{groupId}", produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun getInternalGroupExpenses(
        @PathVariable groupId: String,
    ): InternalGroupExpensesResponse {
        return expenseService.getInternalGroupExpenses(groupId).toInternalGroupExpensesResponse(groupId)
    }

    @GetMapping("activities/groups/{groupId}", produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun getGroupActivities(
        @PathVariable groupId: String,
        @RequestParam title: String?,
        @RequestParam status: ExpenseStatus?,
        @RequestParam creatorId: String?,
        @RequestParam sortedBy: SortedBy,
        @RequestParam sortOrder: SortOrder,
    ): GroupActivitiesResponse {
        val filterOptions = FilterOptions(title, status, creatorId, sortedBy, sortOrder)
        return expenseService.getGroupActivities(groupId, filterOptions).toGroupActivitiesResponse(groupId)
    }
}
