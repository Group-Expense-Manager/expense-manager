package pl.edu.agh.gem.external.dto.group

import pl.edu.agh.gem.internal.model.currency.Currencies
import pl.edu.agh.gem.internal.model.group.GroupOptions

data class GroupOptionsResponse(
    val acceptRequired: Boolean,
    val currencies: Currencies,
) {
    fun toDomain() = GroupOptions(
        acceptRequired = acceptRequired,
        currencies = currencies,
    )
}
