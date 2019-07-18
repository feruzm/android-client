package io.golos.domain.rules

import io.golos.abi.implementation.publish.CreatemssgPublishStruct
import io.golos.abi.implementation.publish.DeletemssgPublishStruct
import io.golos.abi.implementation.publish.UpdatemssgPublishStruct
import io.golos.cyber4j.model.*
import io.golos.cyber4j.utils.toCyberName
import io.golos.domain.Regexps
import io.golos.domain.entities.*
import io.golos.domain.requestmodel.*
import javax.inject.Inject

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-04-02.
 */
class RequestEntityToArgumentsMapper
@Inject
constructor() : EntityToCyberMapper<DiscussionCreationRequestEntity, DiscussionCreateRequest> {

    override suspend fun invoke(entity: DiscussionCreationRequestEntity): DiscussionCreateRequest {
        val tags = HashSet<String>()
        val links = HashSet<String>()

        return when (entity) {
            is PostCreationRequestEntity -> {
                tags.addAll(entity.tags)
                extractHashTags(tags, entity.originalBody.toString())
                extractLinks(links, entity.originalBody.toString())

                CreatePostRequest(
                    entity.title,
                    entity.body,
                    tags.map { Tag(it) },
                    DiscussionCreateMetadata((links + entity.images).map { EmbedmentsUrl(it) }),
                    emptyList(),
                    true,
                    0
                )
            }
            is CommentCreationRequestEntity -> {
                tags.addAll(entity.tags)
                extractHashTags(tags, entity.body)
                extractLinks(links, entity.body)

                CreateCommentRequest(
                    entity.body,
                    entity.parentId.userId.toCyberName(),
                    entity.parentId.permlink,
                    tags.map { Tag(it) },
                    DiscussionCreateMetadata(links.map { EmbedmentsUrl(it) }),
                    emptyList(),
                    true,
                    0
                )
            }
            is DeleteDiscussionRequestEntity -> {
                DeleteDiscussionRequest(entity.discussionPermlink)
            }
            is PostUpdateRequestEntity -> {
                tags.addAll(entity.tags)
                extractHashTags(tags, entity.originalBody.toString())
                extractLinks(links, entity.originalBody.toString())

                UpdatePostRequest(
                    entity.postPermlink,
                    entity.title, entity.body,
                    tags.map { Tag(it) },
                    DiscussionCreateMetadata((links + entity.images).map { EmbedmentsUrl(it) })
                )
            }
        }


    }

    private fun extractHashTags(toContainer: MutableCollection<String>, fromSource: String): MutableCollection<String> {

        Regexps.hashTagRegexp.findAll(fromSource, 0).forEach { matchResult ->

            matchResult.groupValues.firstOrNull()?.takeIf { it.isNotEmpty() }?.apply {

                toContainer.add(this.removePrefix("#"))
            }
        }
        return toContainer
    }

    private fun extractLinks(toContainer: MutableCollection<String>, fromSource: String): MutableCollection<String> {

        Regexps.link.findAll(fromSource, 0).forEach { matchResult ->

            matchResult.groupValues.firstOrNull()?.takeIf { it.isNotEmpty() }?.apply {

                toContainer.add(this.trim())
            }
        }
        return toContainer
    }
}


class DiscussionCreateResultToEntityMapper
@Inject
constructor() : CyberToEntityMapper<CreatemssgPublishStruct, DiscussionCreationResultEntity> {
    override suspend fun invoke(cyberObject: CreatemssgPublishStruct): DiscussionCreationResultEntity {
        return when (cyberObject.parent_id.author.name.orEmpty().isEmpty()) {

            true -> PostCreationResultEntity(
                DiscussionIdEntity(
                    cyberObject.message_id.author.name,
                    cyberObject.message_id.permlink
                )
            )
            false -> CommentCreationResultEntity(
                DiscussionIdEntity(
                    cyberObject.message_id.author.name,
                    cyberObject.message_id.permlink
                ),
                DiscussionIdEntity(
                    cyberObject.parent_id.author.name,
                    cyberObject.parent_id.permlink
                )
            )
        }
    }
}


class DiscussionUpdateResultToEntityMapper
@Inject
constructor() : CyberToEntityMapper<UpdatemssgPublishStruct, UpdatePostResultEntity> {
    override suspend fun invoke(cyberObject: UpdatemssgPublishStruct): UpdatePostResultEntity {
        return UpdatePostResultEntity(
            DiscussionIdEntity(
                cyberObject.message_id.author.name,
                cyberObject.message_id.permlink
            )
        )
    }
}


class DiscussionDeleteResultToEntityMapper
@Inject
constructor() : CyberToEntityMapper<DeletemssgPublishStruct, DeleteDiscussionResultEntity> {
    override suspend fun invoke(cyberObject: DeletemssgPublishStruct): DeleteDiscussionResultEntity {
        return DeleteDiscussionResultEntity(
            DiscussionIdEntity(
                cyberObject.message_id.author.name,
                cyberObject.message_id.permlink
            )
        )
    }
}