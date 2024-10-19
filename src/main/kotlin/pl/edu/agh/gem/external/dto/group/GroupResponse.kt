package pl.edu.agh.gem.external.dto.group

import pl.edu.agh.gem.internal.model.currency.Currency
import pl.edu.agh.gem.internal.model.group.GroupData
import pl.edu.agh.gem.model.GroupMember
import pl.edu.agh.gem.model.GroupMembers

data class GroupResponse(
    val members: List<MemberDTO>,
    val groupCurrencies: List<CurrencyDTO>,
) {
    fun toDomain() = GroupData(
        members = GroupMembers(members.map { GroupMember(it.id) }),
        currencies = groupCurrencies.map { Currency(it.code) },
    )
}

data class MemberDTO(
    val id: String,
)

data class CurrencyDTO(
    val code: String,
)

fun Currency.toDto() = CurrencyDTO(code)
