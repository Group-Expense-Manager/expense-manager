package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.client.CurrencyManagerClient
import pl.edu.agh.gem.internal.client.FinanceAdapterClient
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.mapper.CreditorUserExpenseMapper
import pl.edu.agh.gem.internal.mapper.DebtorUserExpenseMapper
import pl.edu.agh.gem.internal.model.currency.Currency
import pl.edu.agh.gem.internal.model.expense.Expense
import pl.edu.agh.gem.internal.model.expense.ExpenseAction.EDITED
import pl.edu.agh.gem.internal.model.expense.ExpenseCreation
import pl.edu.agh.gem.internal.model.expense.ExpenseDecision
import pl.edu.agh.gem.internal.model.expense.ExpenseHistoryEntry
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.ExpenseUpdate
import pl.edu.agh.gem.internal.model.expense.FxData
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
import java.time.Instant.now
import java.time.ZoneId

@Service
class ExpenseService(
    private val groupManagerClient: GroupManagerClient,
    private val currencyManagerClient: CurrencyManagerClient,
    private val financeAdapterClient: FinanceAdapterClient,
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

    fun create(
        groupData: GroupData,
        expenseCreation: ExpenseCreation,
    ): Expense {
        val dataWrapper = createExpenseCreationDataWrapper(groupData, expenseCreation)

        validate(dataWrapper, costValidator)
            .alsoValidate(dataWrapper, participantValidator)
            .alsoValidate(dataWrapper, currenciesValidator)
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }

        val fxData = createFxData(expenseCreation)

        return expenseRepository.save(
            expenseCreation.toExpense(
                fxData = fxData,
            ),
        )
    }

    private fun createFxData(expenseCreation: ExpenseCreation) =
        expenseCreation.targetCurrency?.let {
            FxData(
                targetCurrency = expenseCreation.targetCurrency,
                exchangeRate =
                    currencyManagerClient.getExchangeRate(
                        expenseCreation.amount.currency,
                        expenseCreation.targetCurrency,
                        expenseCreation.expenseDate.atZone(ZoneId.systemDefault()).toLocalDate(),
                    ),
            )
        }

    private fun createExpenseCreationDataWrapper(
        groupData: GroupData,
        expenseCreation: ExpenseCreation,
    ): ExpenseCreationDataWrapper {
        return ExpenseCreationDataWrapper(
            currencyData =
                CurrencyData(
                    groupCurrencies = groupData.currencies,
                    currencyManagerClient.getAvailableCurrencies(),
                    baseCurrency = expenseCreation.amount.currency,
                    targetCurrency = expenseCreation.targetCurrency,
                ),
            costData =
                CostData(
                    fullCost = expenseCreation.amount.value,
                    partialCosts = expenseCreation.expenseParticipantsCost.map { it.participantCost },
                ),
            participantData =
                ParticipantData(
                    creatorId = expenseCreation.creatorId,
                    participantsId = expenseCreation.expenseParticipantsCost.map { it.participantId },
                    groupMembers = groupData.members,
                ),
        )
    }

    fun getExpense(
        expenseId: String,
        groupId: String,
    ): Expense {
        return expenseRepository.findByExpenseIdAndGroupId(expenseId, groupId) ?: throw MissingExpenseException(expenseId, groupId)
    }

    fun getAcceptedGroupExpenses(
        groupId: String,
        currency: String,
    ): List<Expense> {
        return expenseRepository.findByGroupId(groupId).filter {
            it.status == ACCEPTED && (it.fxData?.targetCurrency ?: it.amount.currency) == currency
        }
    }

    fun getGroupActivities(
        groupId: String,
        filterOptions: FilterOptions?,
    ): List<Expense> {
        return expenseRepository.findByGroupId(groupId, filterOptions)
    }

    fun decide(expenseDecision: ExpenseDecision): Expense {
        val expense =
            expenseRepository.findByExpenseIdAndGroupId(expenseDecision.expenseId, expenseDecision.groupId)
                ?: throw MissingExpenseException(expenseDecision.expenseId, expenseDecision.groupId)

        val dataWrapper = createExpenseDecisionDataWrapper(expense, expenseDecision)
        validate(dataWrapper, expenseDecisionValidator)
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }

        val updatedExpense = expenseRepository.save(expense.addDecision(expenseDecision))

        val previousStatus = expense.status
        val currentStatus = updatedExpense.status
        if (previousStatus.changedToAccepted(currentStatus) || currentStatus.changedFromAccepted(previousStatus)) {
            generateBalancesAndSettlements(updatedExpense)
        }

        return updatedExpense
    }

    private fun generateBalancesAndSettlements(expense: Expense) {
        financeAdapterClient.generate(
            groupId = expense.groupId,
            currency = Currency(expense.fxData?.targetCurrency ?: expense.amount.currency),
        )
    }

    private fun Expense.addDecision(expenseDecision: ExpenseDecision): Expense {
        val updatedExpenseParticipants =
            expenseParticipants.map {
                it.takeIf { it.participantId == expenseDecision.userId }?.copy(participantStatus = expenseDecision.decision.toExpenseStatus()) ?: it
            }

        val expenseHistoryEntry =
            ExpenseHistoryEntry(
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

    private fun createExpenseDecisionDataWrapper(
        expense: Expense,
        expenseDecision: ExpenseDecision,
    ): ExpenseDecisionDataWrapper {
        return ExpenseDecisionDataWrapper(
            expense = expense,
            expenseDecision = expenseDecision,
        )
    }

    fun deleteExpense(
        expenseId: String,
        groupId: String,
        userId: String,
    ) {
        val expenseToDelete = expenseRepository.findByExpenseIdAndGroupId(expenseId, groupId) ?: throw MissingExpenseException(expenseId, groupId)

        val dataWrapper =
            ExpenseDeletionWrapper(
                creatorData =
                    CreatorData(
                        creatorId = expenseToDelete.creatorId,
                        userId = userId,
                    ),
            )

        validate(dataWrapper, creatorValidator)
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }

        expenseRepository.delete(expenseToDelete)
        archivedExpenseRepository.add(expenseToDelete)

        if (expenseToDelete.status == ACCEPTED) {
            generateBalancesAndSettlements(expenseToDelete)
        }
    }

    fun getUserExpenses(
        groupId: String,
        userId: String,
    ): List<UserExpense> {
        val expenses = expenseRepository.findByGroupId(groupId)

        val costsAsExpenseCreator =
            expenses
                .mapNotNull { creditorUserExpenseMapper.mapToUserExpense(userId, it) }
        val costsAsExpenseMember =
            expenses
                .mapNotNull { debtorUserExpenseMapper.mapToUserExpense(userId, it) }

        return costsAsExpenseCreator + costsAsExpenseMember
    }

    fun updateExpense(
        groupData: GroupData,
        update: ExpenseUpdate,
    ): Expense {
        val originalExpense =
            expenseRepository.findByExpenseIdAndGroupId(update.id, update.groupId)
                ?: throw MissingExpenseException(update.id, update.groupId)

        val dataWrapper = createExpenseUpdateDataWrapper(groupData, update, originalExpense)

        validate(dataWrapper, costValidator)
            .alsoValidate(dataWrapper, participantValidator)
            .alsoValidate(dataWrapper, currenciesValidator)
            .alsoValidate(dataWrapper, creatorValidator)
            .takeIf { it.isNotEmpty() }
            ?.also { throw ValidatorsException(it) }

        val updatedExpense =
            expenseRepository.save(
                originalExpense.copy(
                    fxData = updateFxData(originalExpense = originalExpense, expenseUpdate = update),
                    title = update.title,
                    amount = update.amount,
                    updatedAt = now(),
                    expenseDate = update.expenseDate,
                    expenseParticipants = update.expenseParticipantsCost.map { it.toExpenseParticipant() },
                    status = PENDING,
                    attachmentId = update.attachmentId,
                    history = originalExpense.history + ExpenseHistoryEntry(originalExpense.creatorId, EDITED, now(), update.message),
                ),
            )

        if (originalExpense.status == ACCEPTED) {
            generateBalancesAndSettlements(originalExpense)
        }

        return updatedExpense
    }

    private fun updateFxData(
        originalExpense: Expense,
        expenseUpdate: ExpenseUpdate,
    ): FxData? {
        if (shouldUseOriginalFxData(originalExpense, expenseUpdate)) {
            return originalExpense.fxData
        }
        return expenseUpdate.targetCurrency?.let {
            FxData(
                targetCurrency = expenseUpdate.targetCurrency,
                exchangeRate =
                    currencyManagerClient.getExchangeRate(
                        expenseUpdate.amount.currency,
                        expenseUpdate.targetCurrency,
                        expenseUpdate.expenseDate.atZone(ZoneId.systemDefault()).toLocalDate(),
                    ),
            )
        }
    }

    private fun shouldUseOriginalFxData(
        originalExpense: Expense,
        update: ExpenseUpdate,
    ): Boolean {
        return originalExpense.expenseDate == update.expenseDate && originalExpense.amount.currency == update.amount.currency &&
            originalExpense.fxData?.targetCurrency == update.targetCurrency
    }

    private fun createExpenseUpdateDataWrapper(
        groupData: GroupData,
        expenseUpdate: ExpenseUpdate,
        originalExpense: Expense,
    ) = ExpenseUpdateDataWrapper(
        currencyData =
            CurrencyData(
                groupCurrencies = groupData.currencies,
                currencyManagerClient.getAvailableCurrencies(),
                baseCurrency = expenseUpdate.amount.currency,
                targetCurrency = expenseUpdate.targetCurrency,
            ),
        costData =
            CostData(
                fullCost = expenseUpdate.amount.value,
                partialCosts = expenseUpdate.expenseParticipantsCost.map { it.participantCost },
            ),
        participantData =
            ParticipantData(
                creatorId = expenseUpdate.userId,
                participantsId = expenseUpdate.expenseParticipantsCost.map { it.participantId },
                groupMembers = groupData.members,
            ),
        creatorData =
            CreatorData(
                creatorId = originalExpense.creatorId,
                userId = expenseUpdate.userId,
            ),
        originalExpense = originalExpense,
        expenseUpdate = expenseUpdate,
    )
}

class MissingExpenseException(expenseId: String, groupId: String) :
    RuntimeException("Failed to find expense with id: $expenseId and groupId: $groupId")
