package io.golos.domain.dto

import io.golos.domain.use_cases.post.post_dto.ContentBlock

data class PostDomain(
    val author: AuthorDomain,
    val community: CommunityDomain,
    val contentId: ContentIdDomain,
    val body: ContentBlock?,
    val meta: MetaDomain,
    val stats: StatsDomain?,
    val type: String?,
    val shareUrl: String?,
    val votes: VotesDomain,
    val isMyPost: Boolean
)