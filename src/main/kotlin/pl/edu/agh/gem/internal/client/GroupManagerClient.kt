package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.group.Group
import pl.edu.agh.gem.model.GroupMembers

interface GroupManagerClient {
    fun getMembers(groupId: String): GroupMembers
    fun getGroup(groupId: String): Group
}

class GroupManagerClientException(override val message: String?) : RuntimeException()

class RetryableGroupManagerClientException(override val message: String?) : RuntimeException()
