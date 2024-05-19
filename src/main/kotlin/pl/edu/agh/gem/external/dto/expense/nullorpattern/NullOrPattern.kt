package pl.edu.agh.gem.external.dto.expense.nullorpattern

import jakarta.validation.Constraint
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [NullOrPatternValidator::class])
annotation class NullOrPattern(
    val message: String = "{javax.validation.constraints.NullOrPattern.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = [],
)
