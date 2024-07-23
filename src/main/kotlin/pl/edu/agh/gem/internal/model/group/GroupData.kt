package pl.edu.agh.gem.internal.model.group

import pl.edu.agh.gem.internal.model.currency.Currency
import pl.edu.agh.gem.model.GroupMembers

data class GroupData(
    val members: GroupMembers,
    val acceptRequired: Boolean,
    val currencies: Currencies,
)

typealias Currencies = List<Currency>
