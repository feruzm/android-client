package io.golos.cyber_android.ui.dto

import io.golos.domain.use_cases.post.post_dto.PostBlock
import java.util.*

data class Post(

    val author: Author,
    val community: Community,
    val contentId: ContentId,
    val body: PostBlock,
    val meta: Meta,
    val stats: Stats?,
    val type: String?,
    val shareUrl: String?,
    val votes: Votes
) {
    data class Votes(
        val downCount: Long,
        val upCount: Long,
        val hasUpVote: Boolean,
        val hasDownVote: Boolean
    )

    data class Meta(
        val creationTime: Date
    )

    data class Community(
        val alias: String?,
        val communityId: String,
        val name: String?,
        val avatarUrl: String?,
        val isSubscribe: Boolean
    )

    data class Stats(
        val commentsCount: Int
    )

    data class ContentId(
        val communityId: String,
        val permlink: String,
        val userId: String
    )

    data class Author(
        val avatarUrl: String?,
        val userId: String,
        val username: String?
    )
}
