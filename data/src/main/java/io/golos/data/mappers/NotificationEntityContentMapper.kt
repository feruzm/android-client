package io.golos.data.mappers

import io.golos.commun4j.services.model.NotificationEntityContent
import io.golos.domain.dto.*

fun NotificationEntityContent.mapToNotificationCommentDomain(): NotificationCommentDomain {
    val commentContentId = ContentIdDomain(CommunityIdDomain(contentId.communityId?.value ?: ""), this.contentId.permlink, contentId.userId.name)
    val entityContentParents = parents!!
    val parentPostContentId = ContentIdDomain(CommunityIdDomain(entityContentParents.post!!.communityId!!.value), entityContentParents.post!!.permlink, entityContentParents.post!!.userId.name!!)
    val parentCommentContentId = entityContentParents.comment?.let {
        ContentIdDomain(CommunityIdDomain(it.communityId!!.value), it.permlink, it.userId.name!!)
    }
    val parentsDomain = NotificationCommentParentsDomain(parentPostContentId, parentCommentContentId)
    return NotificationCommentDomain(commentContentId, shortText, imageUrl, parentsDomain)
}


fun NotificationEntityContent.mapToNotificationPostDomain(): NotificationPostDomain {
    val commentContentId = ContentIdDomain(CommunityIdDomain(contentId.communityId?.value ?: ""), this.contentId.permlink, contentId.userId.name)
    return NotificationPostDomain(commentContentId, shortText, imageUrl)
}