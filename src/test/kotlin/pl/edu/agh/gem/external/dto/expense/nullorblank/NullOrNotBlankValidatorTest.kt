package pl.edu.agh.gem.external.dto.expense.nullorblank

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.ConstraintValidatorContext
import org.mockito.kotlin.mock

class NullOrNotBlankValidatorTest : ShouldSpec({
    should("accept null value") {
        // given
        val nullString: String? = null
        val validator = NullOrNotBlankValidator()
        val constraintValidatorContext = mock<ConstraintValidatorContext>()

        // given
        val result = validator.isValid(nullString, constraintValidatorContext)

        // then
        result shouldBe true
    }

    should("accept not blank value") {
        // given
        val nullString = "value"
        val validator = NullOrNotBlankValidator()
        val constraintValidatorContext = mock<ConstraintValidatorContext>()

        // given
        val result = validator.isValid(nullString, constraintValidatorContext)

        // then
        result shouldBe true
    }

    should("not accept not null and blank value") {
        // given
        val nullString = ""
        val validator = NullOrNotBlankValidator()
        val constraintValidatorContext = mock<ConstraintValidatorContext>()

        // given
        val result = validator.isValid(nullString, constraintValidatorContext)

        // then
        result shouldBe false
    }
},)
