package pl.edu.agh.gem.internal.model

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.PENDING
import pl.edu.agh.gem.internal.model.expense.ExpenseStatus.REJECTED

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
},)
