package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.client.CurrencyManagerClient
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.mapper.CreditorUserExpenseMapper
import pl.edu.agh.gem.internal.mapper.DebtorUserExpenseMapper
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseAction.EDITED
import pl.edu.agh.gem.internal.model.expense.ExpenseDecision
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.ExpenseUpdate
import pl.edu.agh.gem.internal.model.expense.StatusHistoryEntry
import pl.edu.agh.gem.internal.model.expense.UserExpense
import pl.edu.agh.gem.internal.model.expense.filter.FilterOptions
import pl.edu.agh.gem.internal.model.expense.toExpenseUpdateParticipant
import pl.edu.agh.gem.internal.model.group.GroupData
import pl.edu.agh.gem.internal.persistence.ArchivedExpenseRepository
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.internal.validation.creation.CostValidator
import pl.edu.agh.gem.internal.validation.creation.CurrenciesValidator
import pl.edu.agh.gem.internal.validation.creation.ExpenseCreationDataWrapper
import pl.edu.agh.gem.internal.validation.creation.ParticipantValidator
import pl.edu.agh.gem.internal.validation.decision.ExpenseDecisionDataWrapper
import pl.edu.agh.gem.internal.validation.decision.ExpenseDecisionValidator
import pl.edu.agh.gem.validator.ValidatorList.Companion.validatorsOf
import pl.edu.agh.gem.validator.ValidatorsException
import java.time.Instant
import java.time.Instant.now

@Service
class ExpenseService(
    private val groupManagerClient: GroupManagerClient,
    private val currencyManagerClient: CurrencyManagerClient,
    private val expenseRepository: ExpenseRepository,
    private val archivedExpenseRepository: ArchivedExpenseRepository,
) {
    private val expenseCreationValidators = validatorsOf(
        CostValidator(),
        ParticipantValidator(),
        CurrenciesValidator(),
    )

    private val expenseDecisionValidators = validatorsOf(
        ExpenseDecisionValidator(),
    )

    private val creditorUserExpenseMapper = CreditorUserExpenseMapper()
    private val debtorUserExpenseMapper = DebtorUserExpenseMapper()

    fun getGroup(groupId: String): GroupData {
        return groupManagerClient.getGroup(groupId)
    }

    fun create(groupData: GroupData, expense: Expense): Expense {
        expenseCreationValidators
            .getFailedValidations(createExpenseCreationDataWrapper(groupData, expense))
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }
        return expenseRepository.save(
            expense.copy(
                exchangeRate = getExchangeRate(expense.baseCurrency, expense.targetCurrency, expense.expenseDate),
            ),
        )
    }

    private fun getExchangeRate(baseCurrency: String, targetCurrency: String?, date: Instant) =
        targetCurrency?.let { currencyManagerClient.getExchangeRate(baseCurrency, targetCurrency, date) }

    private fun createExpenseCreationDataWrapper(groupData: GroupData, expense: Expense): ExpenseCreationDataWrapper {
        return ExpenseCreationDataWrapper(
            groupData,
            expense,
            currencyManagerClient.getAvailableCurrencies(),
        )
    }

    fun getExpense(expenseId: String, groupId: String): Expense {
        return expenseRepository.findByExpenseIdAndGroupId(expenseId, groupId) ?: throw MissingExpenseException(expenseId, groupId)
    }

    fun getInternalGroupExpenses(groupId: String): List<Expense> {
        return expenseRepository.findByGroupId(groupId).filter { it.status == ACCEPTED }
    }

    fun getGroupActivities(groupId: String, filterOptions: FilterOptions): List<Expense> {
        return expenseRepository.findByGroupId(groupId, filterOptions)
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

    fun deleteExpense(expenseId: String, groupId: String, userId: String) {
        val expenseToDelete = expenseRepository.findByExpenseIdAndGroupId(expenseId, groupId) ?: throw MissingExpenseException(expenseId, groupId)

        if (!userId.isCreator(expenseToDelete)) {
            throw ExpenseDeletionAccessException(userId, expenseId)
        }

        expenseRepository.delete(expenseToDelete)
        archivedExpenseRepository.add(expenseToDelete)
    }
    private fun String.isCreator(expense: Expense) = expense.creatorId == this

    fun getUserExpenses(groupId: String, userId: String): List<UserExpense> {
        val expenses = expenseRepository.findByGroupId(groupId)

        val costsAsExpenseCreator = expenses
            .mapNotNull { creditorUserExpenseMapper.mapToUserExpense(userId, it) }
        val costsAsExpenseMember = expenses
            .mapNotNull { debtorUserExpenseMapper.mapToUserExpense(userId, it) }

        return costsAsExpenseCreator + costsAsExpenseMember
    }

    fun updateExpense(groupData: GroupData, update: ExpenseUpdate): Expense {
        val originalExpense = expenseRepository.findByExpenseIdAndGroupId(update.id, update.groupId)
            ?: throw MissingExpenseException(update.id, update.groupId)

        if (!update.userId.isCreator(originalExpense)) {
            throw ExpenseUpdateAccessException(update.userId, update.id)
        }

        if (!update.modifies(originalExpense)) {
            throw NoExpenseUpdateException(update.userId, update.id)
        }

        val partiallyUpdatedExpense = originalExpense.update(update)

        expenseCreationValidators
            .getFailedValidations(createExpenseCreationDataWrapper(groupData, partiallyUpdatedExpense))
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }

        return expenseRepository.save(
            partiallyUpdatedExpense.copy(
                exchangeRate = getExchangeRate(
                    partiallyUpdatedExpense.baseCurrency,
                    partiallyUpdatedExpense.targetCurrency,
                    partiallyUpdatedExpense.expenseDate,
                ),
            ),
        )
    }

    private fun ExpenseUpdate.modifies(expense: Expense): Boolean {
        return expense.title != this.title ||
            expense.cost != this.cost ||
            expense.baseCurrency != this.baseCurrency ||
            expense.targetCurrency != this.targetCurrency ||
            expense.expenseDate != this.expenseDate ||
            expense.expenseParticipants.map { it.toExpenseUpdateParticipant() }.toSet() != this.expenseParticipants.toSet()
    }

    private fun Expense.update(expenseUpdate: ExpenseUpdate): Expense {
        return this.copy(
            title = expenseUpdate.title,
            cost = expenseUpdate.cost,
            baseCurrency = expenseUpdate.baseCurrency,
            targetCurrency = expenseUpdate.targetCurrency,
            updatedAt = now(),
            expenseDate = expenseUpdate.expenseDate,
            expenseParticipants = expenseUpdate.expenseParticipants.map { it.toExpenseParticipant(expenseUpdate.userId) },
            status = PENDING,
            statusHistory = statusHistory + StatusHistoryEntry(creatorId, EDITED, now(), expenseUpdate.message),

        )
    }
}

class MissingExpenseException(expenseId: String, groupId: String) :
    RuntimeException("Failed to find expense with id: $expenseId and groupId: $groupId")

class UserNotParticipantException(userId: String, expenseId: String) :
    RuntimeException("User with id: $userId is not a participant of expense with id: $expenseId")

class ExpenseDeletionAccessException(userId: String, expenseId: String) :
    RuntimeException("User with id: $userId can not delete expense with id: $expenseId")

class ExpenseUpdateAccessException(userId: String, expenseId: String) :
    RuntimeException("User with id: $userId can not update expense with id: $expenseId")

class NoExpenseUpdateException(userId: String, expenseId: String) :
    RuntimeException("No update occurred for expense with id: $expenseId by user with id: $userId")
