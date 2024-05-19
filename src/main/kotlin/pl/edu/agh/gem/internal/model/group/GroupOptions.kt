package pl.edu.agh.gem.internal.model.group

import pl.edu.agh.gem.internal.model.currency.Currencies

data class GroupOptions(
    val acceptRequired: Boolean,
    val currencies: Currencies,
)
