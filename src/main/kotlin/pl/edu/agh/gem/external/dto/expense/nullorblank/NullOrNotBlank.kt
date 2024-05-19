package pl.edu.agh.gem.external.dto.expense.nullorblank

import jakarta.validation.Constraint
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [NullOrNotBlankValidator::class])
annotation class NullOrNotBlank(
    val message: String = "{javax.validation.constraints.NullOrNotBlank.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = [],
)
