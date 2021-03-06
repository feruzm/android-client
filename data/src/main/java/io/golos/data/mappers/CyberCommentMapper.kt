package io.golos.data.mappers

import io.golos.commun4j.services.model.CyberCommentRaw
import io.golos.domain.dto.CommentDomain
import io.golos.domain.dto.DonationsDomain
import io.golos.domain.posts_parsing_rendering.mappers.json_to_dto.JsonToDtoMapper

fun CyberCommentRaw.mapToCommentDomain(isMyComment: Boolean, donations: DonationsDomain?): CommentDomain {
    val bodyBlock = this.document?.let { JsonToDtoMapper().map(it) }
    return CommentDomain(
        contentId = this.contentId.mapToContentIdDomain(),
        author = this.author.mapToAuthorDomain(),
        votes = this.votes.mapToVotesDomain(),
        body = bodyBlock,
        jsonBody = null,
        childCommentsCount = this.childCommentsCount,
        community = this.community.mapToCommunityDomain(),
        meta = this.meta.mapToMetaDomain(),
        parent = this.parents.mapToParentCommentDomain(),
        type = this.type,
        isDeleted = this.isDeleted ?: false,
        isMyComment = isMyComment,
        commentLevel = if (this.parents.comment != null) 1 else 0,
        donations = donations
    )
}
