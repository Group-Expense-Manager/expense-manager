package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.group.Group
import pl.edu.agh.gem.internal.model.group.GroupData

interface GroupManagerClient {
    fun getGroup(groupId: String): GroupData
    fun getUserGroups(userId: String): List<Group>
}

class GroupManagerClientException(override val message: String?) : RuntimeException()

class RetryableGroupManagerClientException(override val message: String?) : RuntimeException()
