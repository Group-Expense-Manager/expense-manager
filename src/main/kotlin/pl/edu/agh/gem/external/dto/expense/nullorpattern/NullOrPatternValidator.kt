package pl.edu.agh.gem.external.dto.expense.nullorpattern

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class NullOrPatternValidator : ConstraintValidator<NullOrPattern?, String?> {
    private val pattern = Regex("[A-Z]{3}")

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        return value == null || pattern.matches(value)
    }
}
