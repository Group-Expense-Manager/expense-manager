package pl.edu.agh.gem.external.dto.group

import pl.edu.agh.gem.internal.model.group.Group

data class UserGroupsResponse(
    val groups: List<GroupDto>,
) {
    fun toDomain() = groups.map { Group(it.groupId) }
}

data class GroupDto(
    val groupId: String,
)
