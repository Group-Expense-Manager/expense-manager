package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.attachment.GroupAttachment

interface AttachmentStoreClient {
    fun generateBlankAttachment(groupId: String, userId: String): GroupAttachment
}

class AttachmentStoreClientException(override val message: String?) : RuntimeException()

class RetryableAttachmentStoreClientException(override val message: String?) : RuntimeException()
