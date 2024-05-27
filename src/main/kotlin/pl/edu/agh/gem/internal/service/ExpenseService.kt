package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.client.CurrencyManagerClient
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseDecision
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.StatusHistoryEntry
import pl.edu.agh.gem.internal.model.group.Group
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.internal.validation.creation.CostValidator
import pl.edu.agh.gem.internal.validation.creation.CurrenciesValidator
import pl.edu.agh.gem.internal.validation.creation.ExpenseCreationDataWrapper
import pl.edu.agh.gem.internal.validation.creation.ParticipantValidator
import pl.edu.agh.gem.internal.validation.decision.ExpenseDecisionDataWrapper
import pl.edu.agh.gem.internal.validation.decision.ExpenseDecisionValidator
import pl.edu.agh.gem.model.GroupMembers
import pl.edu.agh.gem.validator.ValidatorList.Companion.validatorsOf
import pl.edu.agh.gem.validator.ValidatorsException
import java.time.Instant
import java.time.Instant.now

@Service
class ExpenseService(
    private val groupManagerClient: GroupManagerClient,
    private val currencyManagerClient: CurrencyManagerClient,
    private val expenseRepository: ExpenseRepository,
) {
    private val expenseCreationValidators = validatorsOf(
        CostValidator(),
        ParticipantValidator(),
        CurrenciesValidator(),
    )

    private val expenseDecisionValidators = validatorsOf(
        ExpenseDecisionValidator(),
    )

    fun getMembers(groupId: String): GroupMembers {
        return groupManagerClient.getMembers(groupId)
    }

    fun getGroup(groupId: String): Group {
        return groupManagerClient.getGroup(groupId)
    }

    fun create(group: Group, expense: Expense): Expense {
        expenseCreationValidators
            .getFailedValidations(createExpenseCreationDataWrapper(group, expense))
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }
        return expenseRepository.create(
            expense.copy(
                exchangeRate = getExchangeRate(expense.baseCurrency, expense.targetCurrency, expense.expenseDate),
            ),
        )
    }

    private fun getExchangeRate(baseCurrency: String, targetCurrency: String?, date: Instant) =
        targetCurrency?.let { currencyManagerClient.getExchangeRate(baseCurrency, targetCurrency, date) }

    private fun createExpenseCreationDataWrapper(group: Group, expense: Expense): ExpenseCreationDataWrapper {
        return ExpenseCreationDataWrapper(
            group,
            expense,
            currencyManagerClient.getAvailableCurrencies(),
        )
    }

    fun getExpense(expenseId: String, groupId: String): Expense {
        return expenseRepository.findByExpenseIdAndGroupId(expenseId, groupId) ?: throw MissingExpenseException(expenseId, groupId)
    }

    fun getGroupExpenses(groupId: String): List<Expense> {
        return expenseRepository.findByGroupId(groupId).also { if (it.isEmpty()) throw GroupWithoutExpenseException(groupId) }
    }

    fun decide(expenseDecision: ExpenseDecision) {
        val expense = expenseRepository.findByExpenseIdAndGroupId(expenseDecision.expenseId, expenseDecision.groupId)
            ?: throw MissingExpenseException(expenseDecision.expenseId, expenseDecision.groupId)

        expenseDecisionValidators
            .getFailedValidations(createExpenseDecisionDataWrapper(expense, expenseDecision))
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }

        expenseRepository.save(expense.addDecision(expenseDecision))
    }

    private fun Expense.addDecision(expenseDecision: ExpenseDecision): Expense {
        expenseParticipants.find { it.participantId == expenseDecision.userId } ?: throw UserNotParticipantException(expenseDecision.userId, id)
        val updatedExpenseParticipants = expenseParticipants.map {
            it.takeIf { it.participantId == expenseDecision.userId }?.copy(participantStatus = expenseDecision.decision.toExpenseStatus()) ?: it
        }

        val statusHistoryEntry = StatusHistoryEntry(
            participantId = expenseDecision.userId,
            expenseAction = expenseDecision.decision.toExpenseAction(),
            comment = expenseDecision.message,
        )
        val updatedStatusHistory = statusHistory + statusHistoryEntry

        return copy(
            updatedAt = now(),
            expenseParticipants = updatedExpenseParticipants,
            status = ExpenseStatus.reduce(updatedExpenseParticipants.map { it.participantStatus }),
            statusHistory = updatedStatusHistory,

        )
    }

    private fun createExpenseDecisionDataWrapper(expense: Expense, expenseDecision: ExpenseDecision): ExpenseDecisionDataWrapper {
        return ExpenseDecisionDataWrapper(
            expense = expense,
            expenseDecision = expenseDecision,
        )
    }
}

class MissingExpenseException(expenseId: String, groupId: String) :
    RuntimeException("Failed to find expense with id: $expenseId and groupId: $groupId")

class GroupWithoutExpenseException(groupId: String) :
    RuntimeException("Group with id: $groupId does not have any expenses")

class UserNotParticipantException(userId: String, expenseId: String) :
    RuntimeException("User with id: $userId is not participant of expense with id: $expenseId")
