package pl.edu.agh.gem.integration.persistence

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.group.DummyGroup.OTHER_GROUP_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.REJECTED
import pl.edu.agh.gem.internal.model.expense.filter.SortOrder.ASCENDING
import pl.edu.agh.gem.internal.model.expense.filter.SortOrder.DESCENDING
import pl.edu.agh.gem.internal.model.expense.filter.SortedBy.DATE
import pl.edu.agh.gem.internal.model.expense.filter.SortedBy.TITLE
import pl.edu.agh.gem.internal.persistence.ExpenseRepository
import pl.edu.agh.gem.util.DummyData.CURRENCY_1
import pl.edu.agh.gem.util.DummyData.CURRENCY_2
import pl.edu.agh.gem.util.createAmount
import pl.edu.agh.gem.util.createExpense
import pl.edu.agh.gem.util.createFilterOptions
import pl.edu.agh.gem.util.createFxData
import java.time.Instant.ofEpochMilli

class MongoExpenseRepositoryIT(
    private val expenseRepository: ExpenseRepository,
) : BaseIntegrationSpec({

        should("delete expense") {
            // given
            val expense = createExpense()
            expenseRepository.save(expense)

            // when
            expenseRepository.delete(expense)

            // then
            expenseRepository.findByExpenseIdAndGroupId(expense.id, expense.groupId).also {
                it.shouldBeNull()
            }
        }

        should("return empty list when there is no expense with given groupId") {
            // given
            val expense = createExpense(groupId = OTHER_GROUP_ID)
            expenseRepository.save(expense)

            // when
            val expenses = expenseRepository.findByGroupId(GROUP_ID)

            // then
            expenses.shouldBeEmpty()
        }

        should("find expense with given groupId") {
            // given
            val expense = createExpense(groupId = GROUP_ID)
            expenseRepository.save(expense)

            // when
            val expenses = expenseRepository.findByGroupId(GROUP_ID)

            // then
            expenses.also {
                it.size shouldBe 1
                it.first().id shouldBe expense.id
            }
        }

        should("find expense with given groupId and containing title") {
            // given
            val expense1 = createExpense(id = "1", groupId = GROUP_ID, title = "Pizza in Krakow")
            val expense2 = createExpense(id = "2", groupId = GROUP_ID, title = "The best burger")
            val expense3 = createExpense(id = "3", groupId = GROUP_ID, title = "Spaghetti with Andrzej")

            listOf(expense1, expense2, expense3).forEach { expenseRepository.save(it) }

            val filterOptions = createFilterOptions(title = "KRA")

            // when
            val expenses = expenseRepository.findByGroupId(GROUP_ID, filterOptions)

            // then
            expenses.also {
                it.size shouldBe 1
                it.first().id shouldBe expense1.id
            }
        }

        should("find expense with given groupId and status") {
            // given
            val expense1 = createExpense(id = "1", groupId = GROUP_ID, status = REJECTED)
            val expense2 = createExpense(id = "2", groupId = GROUP_ID, status = ACCEPTED)
            val expense3 = createExpense(id = "3", groupId = GROUP_ID, status = PENDING)

            listOf(expense1, expense2, expense3).forEach { expenseRepository.save(it) }

            val filterOptions = createFilterOptions(status = PENDING)

            // when
            val expenses = expenseRepository.findByGroupId(GROUP_ID, filterOptions)

            // then
            expenses.also {
                it.size shouldBe 1
                it.first().id shouldBe expense3.id
            }
        }

        should("find expense with given groupId and creatorId") {
            // given
            val expense1 = createExpense(id = "1", groupId = GROUP_ID, creatorId = "1")
            val expense2 = createExpense(id = "2", groupId = GROUP_ID, creatorId = "2")
            val expense3 = createExpense(id = "3", groupId = GROUP_ID, creatorId = "1")

            listOf(expense1, expense2, expense3).forEach { expenseRepository.save(it) }

            val filterOptions = createFilterOptions(creatorId = "1")

            // when
            val expenses = expenseRepository.findByGroupId(GROUP_ID, filterOptions)

            // then
            expenses.map { it.id } shouldContainExactly listOf(expense1.id, expense3.id)
        }

        should("find expense with given groupId and currency") {
            // given
            val expense1 = createExpense(id = "1", groupId = GROUP_ID, amount = createAmount(currency = CURRENCY_1), fxData = null)
            val expense2 = createExpense(id = "2", groupId = GROUP_ID, amount = createAmount(currency = CURRENCY_2), fxData = null)
            val expense3 =
                createExpense(
                    id = "3",
                    groupId = GROUP_ID,
                    amount = createAmount(currency = CURRENCY_2),
                    fxData = createFxData(targetCurrency = CURRENCY_1),
                )
            val expense4 =
                createExpense(
                    id = "4",
                    groupId = GROUP_ID,
                    amount = createAmount(currency = CURRENCY_1),
                    fxData = createFxData(targetCurrency = CURRENCY_2),
                )

            listOf(expense1, expense2, expense3, expense4).forEach { expenseRepository.save(it) }

            val filterOptions = createFilterOptions(currency = CURRENCY_1)

            // when
            val expenses = expenseRepository.findByGroupId(GROUP_ID, filterOptions)

            // then
            expenses.map { it.id } shouldContainExactly listOf(expense1.id, expense3.id)
        }

        should("find expense with given groupId and sorted by title ascending") {
            // given
            val expense1 = createExpense(id = "1", groupId = GROUP_ID, title = "Pizza in Krakow")
            val expense2 = createExpense(id = "2", groupId = GROUP_ID, title = "The best burger")
            val expense3 = createExpense(id = "3", groupId = GROUP_ID, title = "Spaghetti with Andrzej")

            listOf(expense1, expense2, expense3).forEach { expenseRepository.save(it) }

            val filterOptions = createFilterOptions(sortedBy = TITLE, sortOrder = ASCENDING)

            // when
            val expenses = expenseRepository.findByGroupId(GROUP_ID, filterOptions)

            // then
            expenses.also {
                expenses.map { it.id } shouldContainExactly listOf(expense1.id, expense3.id, expense2.id)
            }
        }

        should("find expense with given groupId and sorted by title descending") {
            // given
            val expense1 = createExpense(id = "1", groupId = GROUP_ID, title = "Pizza in Krakow")
            val expense2 = createExpense(id = "2", groupId = GROUP_ID, title = "The best burger")
            val expense3 = createExpense(id = "3", groupId = GROUP_ID, title = "Spaghetti with Andrzej")

            listOf(expense1, expense2, expense3).forEach { expenseRepository.save(it) }

            val filterOptions = createFilterOptions(sortedBy = TITLE, sortOrder = DESCENDING)

            // when
            val expenses = expenseRepository.findByGroupId(GROUP_ID, filterOptions)

            // then
            expenses.also {
                expenses.map { it.id } shouldContainExactly listOf(expense2.id, expense3.id, expense1.id)
            }
        }

        should("find expense with given groupId and sorted by expenseDate ascending") {
            // given
            val expense1 = createExpense(id = "1", groupId = GROUP_ID, expenseDate = ofEpochMilli(2))
            val expense2 = createExpense(id = "2", groupId = GROUP_ID, expenseDate = ofEpochMilli(3))
            val expense3 = createExpense(id = "3", groupId = GROUP_ID, expenseDate = ofEpochMilli(1))

            listOf(expense1, expense2, expense3).forEach { expenseRepository.save(it) }

            val filterOptions = createFilterOptions(sortedBy = DATE, sortOrder = ASCENDING)

            // when
            val expenses = expenseRepository.findByGroupId(GROUP_ID, filterOptions)

            // then
            expenses.also {
                expenses.map { it.id } shouldContainExactly listOf(expense3.id, expense1.id, expense2.id)
            }
        }

        should("find expense with given groupId and sorted by expenseDate descending") {
            // given
            val expense1 = createExpense(id = "1", groupId = GROUP_ID, expenseDate = ofEpochMilli(2))
            val expense2 = createExpense(id = "2", groupId = GROUP_ID, expenseDate = ofEpochMilli(3))
            val expense3 = createExpense(id = "3", groupId = GROUP_ID, expenseDate = ofEpochMilli(1))

            listOf(expense1, expense2, expense3).forEach { expenseRepository.save(it) }

            val filterOptions = createFilterOptions(sortedBy = DATE, sortOrder = DESCENDING)

            // when
            val expenses = expenseRepository.findByGroupId(GROUP_ID, filterOptions)

            // then
            expenses.also {
                expenses.map { it.id } shouldContainExactly listOf(expense2.id, expense1.id, expense3.id)
            }
        }
    })
