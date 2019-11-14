package io.golos.domain.dto

import io.golos.domain.use_cases.post.post_dto.PostBlock
import java.util.*

data class PostDomain(

    val author: AuthorDomain,
    val community: CommunityDomain,
    val contentId: ContentIdDomain,
    val body: PostBlock,
    val meta: MetaDomain,
    val stats: StatsDomain?,
    val type: String?,
    val votes: VotesDomain
) {
    data class VotesDomain(
        val downCount: Long,
        val upCount: Long,
        val hasUpVote: Boolean,
        val hasDownVote: Boolean
    )

    data class MetaDomain(
        val creationTime: Date
    )

    data class DocumentDomain(
        val attributes: AttributesDomain,
        val content: List<ContentDomain>,
        val id: String,
        val type: String
    ) {
        data class ContentDomain(
            val contentBodyList: List<ContentBodyDomain>,
            val id: String,
            val type: String
        ) {
            data class ContentBodyDomain(
                val content: String,
                val id: String,
                val type: String
            )
        }

        data class AttributesDomain(
            val type: String,
            val version: String
        )
    }

    data class CommunityDomain(
        val alias: String?,
        val communityId: String,
        val name: String?,
        val avatarUrl: String?
    )

    data class StatsDomain(
        val commentsCount: Int
    )

    data class ContentIdDomain(
        val communityId: String,
        val permlink: String,
        val userId: String
    )

    data class AuthorDomain(
        val avatarUrl: String?,
        val userId: String,
        val username: String?
    )
}
