package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.client.AttachmentStoreClient
import pl.edu.agh.gem.internal.client.CurrencyManagerClient
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.mapper.CreditorUserExpenseMapper
import pl.edu.agh.gem.internal.mapper.DebtorUserExpenseMapper
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseAction.EDITED
import pl.edu.agh.gem.internal.model.expense.ExpenseCreation
import pl.edu.agh.gem.internal.model.expense.ExpenseDecision
import pl.edu.agh.gem.internal.model.expense.ExpenseHistoryEntry
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.ExpenseUpdate
import pl.edu.agh.gem.internal.model.expense.UserExpense
import pl.edu.agh.gem.internal.model.expense.filter.FilterOptions
import pl.edu.agh.gem.internal.model.group.GroupData
import pl.edu.agh.gem.internal.persistence.ArchivedExpenseRepository
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.internal.validation.cost.CostData
import pl.edu.agh.gem.internal.validation.cost.CostValidator
import pl.edu.agh.gem.internal.validation.creation.ExpenseCreationDataWrapper
import pl.edu.agh.gem.internal.validation.creator.CreatorData
import pl.edu.agh.gem.internal.validation.creator.CreatorValidator
import pl.edu.agh.gem.internal.validation.currency.CurrenciesValidator
import pl.edu.agh.gem.internal.validation.currency.CurrencyData
import pl.edu.agh.gem.internal.validation.decision.ExpenseDecisionDataWrapper
import pl.edu.agh.gem.internal.validation.decision.ExpenseDecisionValidator
import pl.edu.agh.gem.internal.validation.deletion.ExpenseDeletionWrapper
import pl.edu.agh.gem.internal.validation.participant.ParticipantData
import pl.edu.agh.gem.internal.validation.participant.ParticipantValidator
import pl.edu.agh.gem.internal.validation.update.ExpenseUpdateDataWrapper
import pl.edu.agh.gem.validator.ValidatorsException
import pl.edu.agh.gem.validator.alsoValidate
import pl.edu.agh.gem.validator.validate
import java.time.Instant
import java.time.Instant.now

@Service
class ExpenseService(
    private val groupManagerClient: GroupManagerClient,
    private val currencyManagerClient: CurrencyManagerClient,
    private val attachmentStoreClient: AttachmentStoreClient,
    private val expenseRepository: ExpenseRepository,
    private val archivedExpenseRepository: ArchivedExpenseRepository,
) {

    val costValidator = CostValidator()
    val participantValidator = ParticipantValidator()
    val currenciesValidator = CurrenciesValidator()
    val creatorValidator = CreatorValidator()

    private val expenseDecisionValidator = ExpenseDecisionValidator()

    private val creditorUserExpenseMapper = CreditorUserExpenseMapper()
    private val debtorUserExpenseMapper = DebtorUserExpenseMapper()

    fun getGroup(groupId: String): GroupData {
        return groupManagerClient.getGroup(groupId)
    }

    fun create(groupData: GroupData, expenseCreation: ExpenseCreation): Expense {
        val dataWrapper = createExpenseCreationDataWrapper(groupData, expenseCreation)

        validate(dataWrapper, costValidator)
            .alsoValidate(dataWrapper, participantValidator)
            .alsoValidate(dataWrapper, currenciesValidator)
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }

        val attachmentId = expenseCreation.attachmentId
            ?: attachmentStoreClient.generateBlankAttachment(expenseCreation.groupId, expenseCreation.creatorId).id

        return expenseRepository.save(
            expenseCreation.toExpense(
                exchangeRate = getExchangeRate(expenseCreation.baseCurrency, expenseCreation.targetCurrency, expenseCreation.expenseDate),
                attachmentId = attachmentId,
            ),
        )
    }

    private fun getExchangeRate(baseCurrency: String, targetCurrency: String?, date: Instant) =
        targetCurrency?.let { currencyManagerClient.getExchangeRate(baseCurrency, targetCurrency, date) }

    private fun createExpenseCreationDataWrapper(groupData: GroupData, expenseCreation: ExpenseCreation): ExpenseCreationDataWrapper {
        return ExpenseCreationDataWrapper(
            currencyData = CurrencyData(
                groupCurrencies = groupData.currencies,
                currencyManagerClient.getAvailableCurrencies(),
                baseCurrency = expenseCreation.baseCurrency,
                targetCurrency = expenseCreation.targetCurrency,
            ),
            costData = CostData(
                fullCost = expenseCreation.cost,
                partialCosts = expenseCreation.expenseParticipantsCost.map { it.participantCost },
            ),
            participantData = ParticipantData(
                creatorId = expenseCreation.creatorId,
                participantsId = expenseCreation.expenseParticipantsCost.map { it.participantId },
                groupMembers = groupData.members,
            ),
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

        val dataWrapper = createExpenseDecisionDataWrapper(expense, expenseDecision)
        validate(dataWrapper, expenseDecisionValidator)
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }

        expenseRepository.save(expense.addDecision(expenseDecision))
    }

    private fun Expense.addDecision(expenseDecision: ExpenseDecision): Expense {
        val updatedExpenseParticipants = expenseParticipants.map {
            it.takeIf { it.participantId == expenseDecision.userId }?.copy(participantStatus = expenseDecision.decision.toExpenseStatus()) ?: it
        }

        val expenseHistoryEntry = ExpenseHistoryEntry(
            participantId = expenseDecision.userId,
            expenseAction = expenseDecision.decision.toExpenseAction(),
            comment = expenseDecision.message,
        )
        val updatedHistory = history + expenseHistoryEntry

        return copy(
            updatedAt = now(),
            expenseParticipants = updatedExpenseParticipants,
            status = ExpenseStatus.reduce(updatedExpenseParticipants.map { it.participantStatus }),
            history = updatedHistory,

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

        val dataWrapper = ExpenseDeletionWrapper(
            creatorData = CreatorData(
                creatorId = expenseToDelete.creatorId,
                userId = userId,
            ),
        )

        validate(dataWrapper, creatorValidator)
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }

        expenseRepository.delete(expenseToDelete)
        archivedExpenseRepository.add(expenseToDelete)
    }

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

        val dataWrapper = createExpenseUpdateDataWrapper(groupData, update, originalExpense)

        validate(dataWrapper, costValidator)
            .alsoValidate(dataWrapper, participantValidator)
            .alsoValidate(dataWrapper, currenciesValidator)
            .alsoValidate(dataWrapper, creatorValidator)
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }

        return expenseRepository.save(
            originalExpense.copy(
                exchangeRate = getExchangeRate(
                    update.baseCurrency,
                    update.targetCurrency,
                    update.expenseDate,
                ),
                title = update.title,
                cost = update.cost,
                baseCurrency = update.baseCurrency,
                targetCurrency = update.targetCurrency,
                updatedAt = now(),
                expenseDate = update.expenseDate,
                expenseParticipants = update.expenseParticipantsCost.map { it.toExpenseParticipant(update.userId) },
                status = PENDING,
                history = originalExpense.history + ExpenseHistoryEntry(originalExpense.creatorId, EDITED, now(), update.message),
            ),
        )
    }
    private fun createExpenseUpdateDataWrapper(
        groupData: GroupData,
        expenseUpdate: ExpenseUpdate,
        originalExpense: Expense,
    ) =
        ExpenseUpdateDataWrapper(
            currencyData = CurrencyData(
                groupCurrencies = groupData.currencies,
                currencyManagerClient.getAvailableCurrencies(),
                baseCurrency = expenseUpdate.baseCurrency,
                targetCurrency = expenseUpdate.targetCurrency,
            ),
            costData = CostData(
                fullCost = expenseUpdate.cost,
                partialCosts = expenseUpdate.expenseParticipantsCost.map { it.participantCost },
            ),
            participantData = ParticipantData(
                creatorId = expenseUpdate.userId,
                participantsId = expenseUpdate.expenseParticipantsCost.map { it.participantId },
                groupMembers = groupData.members,
            ),
            creatorData = CreatorData(
                creatorId = originalExpense.creatorId,
                userId = expenseUpdate.userId,
            ),
            originalExpense = originalExpense,
            expenseUpdate = expenseUpdate,
        )
}

class MissingExpenseException(expenseId: String, groupId: String) :
    RuntimeException("Failed to find expense with id: $expenseId and groupId: $groupId")
