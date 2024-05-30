package pl.edu.agh.gem.external.dto.group

import pl.edu.agh.gem.internal.model.currency.Currency
import pl.edu.agh.gem.internal.model.group.Group
import pl.edu.agh.gem.model.GroupMember
import pl.edu.agh.gem.model.GroupMembers

data class GroupResponse(
    val members: List<String>,
    val acceptRequired: Boolean,
    val currencies: List<String>,
) {
    fun toDomain() = Group(
        members = GroupMembers(members.map { GroupMember(it) }),
        acceptRequired = acceptRequired,
        currencies = currencies.map { Currency(it) },
    )
}
