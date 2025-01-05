package pl.edu.agh.gem.internal.model

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.REJECTED
import pl.edu.agh.gem.util.Triple

class ExpenseStatusTest : ShouldSpec({

    context("reduce correctly") {
        withData(
            Pair(listOf(REJECTED, ACCEPTED, PENDING), REJECTED),
            Pair(listOf(ACCEPTED, ACCEPTED, ACCEPTED), ACCEPTED),
            Pair(listOf(ACCEPTED, PENDING, ACCEPTED), PENDING),
        ) { (statuses, expectedStatus) ->
            // when
            val result = ExpenseStatus.reduce(statuses)

            // then
            result shouldBe expectedStatus
        }
    }

    context("return changedToAccepted correctly") {
        withData(
            Triple(ACCEPTED, ACCEPTED, false),
            Triple(ACCEPTED, REJECTED, false),
            Triple(ACCEPTED, PENDING, false),
            Triple(REJECTED, ACCEPTED, true),
            Triple(REJECTED, REJECTED, false),
            Triple(REJECTED, PENDING, false),
            Triple(PENDING, ACCEPTED, true),
            Triple(PENDING, REJECTED, false),
            Triple(PENDING, PENDING, false),
        ) { (previous, current, expectedStatus) ->
            // when
            val result = previous.changedToAccepted(current)

            // then
            result shouldBe expectedStatus
        }
    }

    context("return changedFromAccepted correctly") {
        withData(
            Triple(ACCEPTED, ACCEPTED, false),
            Triple(ACCEPTED, REJECTED, true),
            Triple(ACCEPTED, PENDING, true),
            Triple(REJECTED, ACCEPTED, false),
            Triple(REJECTED, REJECTED, false),
            Triple(REJECTED, PENDING, false),
            Triple(PENDING, ACCEPTED, false),
            Triple(PENDING, REJECTED, false),
            Triple(PENDING, PENDING, false),
        ) { (previous, current, expectedStatus) ->
            // when
            val result = current.changedFromAccepted(previous)

            // then
            result shouldBe expectedStatus
        }
    }
})
