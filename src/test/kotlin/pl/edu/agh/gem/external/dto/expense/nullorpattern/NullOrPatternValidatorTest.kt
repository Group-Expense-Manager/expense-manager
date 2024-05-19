package pl.edu.agh.gem.external.dto.expense.nullorpattern

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.ConstraintValidatorContext
import org.mockito.kotlin.mock

class NullOrPatternValidatorTest : ShouldSpec({
    should("accept null value") {
        // given
        val nullString: String? = null
        val validator = NullOrPatternValidator()
        val constraintValidatorContext = mock<ConstraintValidatorContext>()

        // given
        val result = validator.isValid(nullString, constraintValidatorContext)

        // then
        result shouldBe true
    }

    should("accept string with pattern") {
        // given
        val nullString = "ABC"
        val validator = NullOrPatternValidator()
        val constraintValidatorContext = mock<ConstraintValidatorContext>()

        // given
        val result = validator.isValid(nullString, constraintValidatorContext)

        // then
        result shouldBe true
    }

    should("reject string withou pattern") {
        // given
        val nullString = "abc"
        val validator = NullOrPatternValidator()
        val constraintValidatorContext = mock<ConstraintValidatorContext>()

        // given
        val result = validator.isValid(nullString, constraintValidatorContext)

        // then
        result shouldBe false
    }
},)
