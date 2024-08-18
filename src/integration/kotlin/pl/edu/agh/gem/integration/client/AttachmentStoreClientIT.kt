package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubAttachmentStoreGenerateBlankAttachment
import pl.edu.agh.gem.internal.client.AttachmentStoreClient
import pl.edu.agh.gem.internal.client.AttachmentStoreClientException
import pl.edu.agh.gem.internal.client.RetryableAttachmentStoreClientException
import pl.edu.agh.gem.util.createGroupAttachmentResponse

class AttachmentStoreClientIT(
    private val attachmentStoreClient: AttachmentStoreClient,
) : BaseIntegrationSpec({
    should("get blank attachment") {
        // given
        val attachment = createGroupAttachmentResponse()
        stubAttachmentStoreGenerateBlankAttachment(attachment, GROUP_ID, USER_ID)

        // when
        val result = attachmentStoreClient.generateBlankAttachment(GROUP_ID, USER_ID)

        // then
        result.id shouldBe attachment.id
    }

    should("throw AttachmentStoreClientException when we send bad request") {
        // given
        val attachment = createGroupAttachmentResponse()

        stubAttachmentStoreGenerateBlankAttachment(attachment, GROUP_ID, USER_ID, NOT_ACCEPTABLE)

        // when & then
        shouldThrow<AttachmentStoreClientException> {
            attachmentStoreClient.generateBlankAttachment(GROUP_ID, USER_ID)
        }
    }

    should("throw RetryableAttachmentStoreClientException when client has internal error") {
        // given
        val attachment = createGroupAttachmentResponse()

        stubAttachmentStoreGenerateBlankAttachment(attachment, GROUP_ID, USER_ID, INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableAttachmentStoreClientException> {
            attachmentStoreClient.generateBlankAttachment(GROUP_ID, USER_ID)
        }
    }
},)
