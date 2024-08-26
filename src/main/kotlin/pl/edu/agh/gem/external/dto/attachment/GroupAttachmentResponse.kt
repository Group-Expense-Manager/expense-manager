package pl.edu.agh.gem.external.dto.attachment

import pl.edu.agh.gem.internal.model.attachment.GroupAttachment

data class GroupAttachmentResponse(
    val id: String,
)
fun GroupAttachmentResponse.toDomain() =
    GroupAttachment(
        id = id,
    )
