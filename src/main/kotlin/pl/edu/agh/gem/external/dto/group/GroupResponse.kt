package pl.edu.agh.gem.external.dto.group

import pl.edu.agh.gem.internal.model.currency.Currency
import pl.edu.agh.gem.internal.model.group.Group
import pl.edu.agh.gem.model.GroupMember
import pl.edu.agh.gem.model.GroupMembers

data class GroupResponse(
    val members: List<MemberDTO>,
    val acceptRequired: Boolean,
    val groupCurrencies: List<CurrencyDTO>,
) {
    fun toDomain() = Group(
        members = GroupMembers(members.map { GroupMember(it.id) }),
        acceptRequired = acceptRequired,
        currencies = groupCurrencies.map { Currency(it.code) },
    )
}

data class MemberDTO(
    val id: String,
)

data class CurrencyDTO(
    val code: String,
)
