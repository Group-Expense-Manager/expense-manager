package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.group.GroupOptions
import pl.edu.agh.gem.model.GroupMembers

interface GroupManagerClient {
    fun getGroupMembers(groupId: String): GroupMembers
    fun getGroupOptions(groupId: String): GroupOptions
}

class GroupManagerClientException(override val message: String?) : RuntimeException()

class RetryableGroupManagerClientException(override val message: String?) : RuntimeException()
