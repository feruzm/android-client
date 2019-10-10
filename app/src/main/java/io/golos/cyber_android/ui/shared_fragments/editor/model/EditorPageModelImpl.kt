package io.golos.cyber_android.ui.shared_fragments.editor.model

import android.net.Uri
import io.golos.commun4j.services.model.OEmbedResult
import io.golos.commun4j.sharedmodel.Either
import io.golos.cyber_android.application.App
import io.golos.cyber_android.ui.common.mvvm.model.ModelBaseImpl
import io.golos.cyber_android.ui.shared_fragments.editor.dto.ExternalLinkError
import io.golos.cyber_android.ui.shared_fragments.editor.dto.ExternalLinkInfo
import io.golos.cyber_android.ui.shared_fragments.editor.dto.ExternalLinkType
import io.golos.cyber_android.ui.shared_fragments.editor.dto.ValidationResult
import io.golos.cyber_android.utils.PostConstants
import io.golos.data.api.communities.CommunitiesApi
import io.golos.data.api.embed.EmbedApi
import io.golos.data.errors.CyberServicesError
import io.golos.data.repositories.discussion_creation.DiscussionCreationRepository
import io.golos.data.repositories.images_uploading.ImageUploadRepository
import io.golos.domain.DispatchersProvider
import io.golos.domain.KeyValueStorageFacade
import io.golos.domain.commun_entities.Community
import io.golos.domain.commun_entities.CommunityId
import io.golos.domain.entities.PostCreationResultEntity
import io.golos.domain.entities.UpdatePostResultEntity
import io.golos.domain.entities.UploadedImageEntity
import io.golos.domain.interactors.model.DiscussionCreationResultModel
import io.golos.domain.interactors.model.DiscussionIdModel
import io.golos.domain.interactors.model.PostCreationResultModel
import io.golos.domain.interactors.model.UpdatePostResultModel
import io.golos.domain.post.editor_output.*
import io.golos.domain.posts_parsing_rendering.mappers.editor_output_to_json.EditorOutputToJsonMapper
import io.golos.domain.requestmodel.CompressionParams
import io.golos.domain.requestmodel.ImageUploadRequest
import io.golos.domain.requestmodel.PostCreationRequestEntity
import io.golos.domain.requestmodel.PostUpdateRequestEntity
import io.golos.posts_editor.utilities.post.PostStubs
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import javax.inject.Inject

class EditorPageModelImpl
@Inject
constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val embedApi: EmbedApi,
    private val imageUploadRepository: ImageUploadRepository,
    private val discussionCreationRepository: DiscussionCreationRepository,
    private val communityApi: CommunitiesApi,
    private val keyValueStorage: KeyValueStorageFacade
) : ModelBaseImpl(), EditorPageModel {

    override suspend fun getExternalLinkInfo(uri: String): Either<ExternalLinkInfo, ExternalLinkError> =
        withContext(dispatchersProvider.ioDispatcher) {
            try {
                val linkInfo = mapExternalLinkInfo(embedApi.getOEmbedEmbed(uri), uri)
                if(linkInfo == null) {
                    Either.Failure<ExternalLinkInfo, ExternalLinkError>(ExternalLinkError.TYPE_IS_NOT_SUPPORTED)
                } else {
                    Either.Success<ExternalLinkInfo, ExternalLinkError>(linkInfo)
                }
            } catch (ex: CyberServicesError) {
                App.logger.log(ex)
                Either.Failure<ExternalLinkInfo, ExternalLinkError>(ExternalLinkError.INVALID_URL)
            }
            catch (ex: Exception) {
                App.logger.log(ex)
                Either.Failure<ExternalLinkInfo, ExternalLinkError>(ExternalLinkError.GENERAL_ERROR)
            }
        }

    override fun validatePost(title: String, content: List<ControlMetadata>): ValidationResult {
        if(content.isEmpty()) {
            return ValidationResult.ERROR_POST_IS_EMPTY
        }

        val postLen = content
            .filterIsInstance<ParagraphMetadata>()
            .sumBy { it.plainText.length }

        if(postLen > PostConstants.MAX_POST_CONTENT_LENGTH) {
            return ValidationResult.ERROR_POST_IS_TOO_LONG
        }

        return ValidationResult.SUCCESS
    }

    /**
     * @return null if no image to upload otherwise - operation result
     */
    @Suppress("IfThenToElvis")
    override suspend fun uploadLocalImage(content: List<ControlMetadata>): UploadedImageEntity? =
        withContext(dispatchersProvider.ioDispatcher) {
            val firstLocalImage = content.firstOrNull { it is EmbedMetadata && it.type == EmbedType.LOCAL_IMAGE }

            if(firstLocalImage == null) {
                return@withContext null
            } else {
                firstLocalImage
                    .let { metadata -> (metadata as EmbedMetadata).sourceUri }
                    .let { uri ->
                        val file = File(URI.create(uri.toString()))
                        imageUploadRepository.upload(ImageUploadRequest(file, uri, CompressionParams.DirectCompressionParams))
                    }
            }
        }

    override suspend fun createPost(
        content: List<ControlMetadata>,
        adultOnly: Boolean,
        communityId: CommunityId,
        localImagesUri: List<String>) : DiscussionCreationResultModel {
        val postText = EditorOutputToJsonMapper.map(content, localImagesUri)

        val tags = extractTags(content, adultOnly)

        val postRequest = PostCreationRequestEntity("", postText, postText, tags.toList(), communityId, localImagesUri)

        return (discussionCreationRepository.createOrUpdate(postRequest) as PostCreationResultEntity)
            .let {
                PostCreationResultModel(DiscussionIdModel(it.postId.userId, it.postId.permlink))
            }
    }

    override suspend fun updatePost(content: List<ControlMetadata>, permlink: String, adultOnly: Boolean, localImagesUri: List<String>)
            : DiscussionCreationResultModel {
        val postText = EditorOutputToJsonMapper.map(content, localImagesUri)

        val tags = extractTags(content, adultOnly)

        val postRequest = PostUpdateRequestEntity(permlink, "", postText, postText, tags.toList(), localImagesUri)

        return (discussionCreationRepository.createOrUpdate(postRequest) as UpdatePostResultEntity)
            .let {
                UpdatePostResultModel(DiscussionIdModel(it.id.userId, it.id.permlink))
            }
    }

    override suspend fun getLastUsedCommunity(): Community? =
        withContext(dispatchersProvider.ioDispatcher) {
            keyValueStorage.getLastUsedCommunityId()?.let {communityApi.getCommunityById(CommunityId(it))}
        }

    override suspend fun saveLastUsedCommunity(community: Community) {
        withContext(dispatchersProvider.ioDispatcher) {
            keyValueStorage.saveLastUsedCommunityId(community.id.id)
        }
    }

    /**
     * @return null - this type of link is not supported
     */
    private fun mapExternalLinkInfo(serverLinkInfo: OEmbedResult, sourceUrl: String): ExternalLinkInfo? {
        val type  = when(serverLinkInfo.type) {
            "link" -> ExternalLinkType.WEBSITE
            "photo" -> ExternalLinkType.IMAGE
            "video" -> ExternalLinkType.VIDEO
            else -> {
                App.logger.log(UnsupportedOperationException("This resource type is not supported: ${serverLinkInfo.type}"))
                null
            }
        }
        ?: return null

        val thumbnailUrl = when(type) {
            ExternalLinkType.VIDEO -> serverLinkInfo.thumbnail_url ?: PostStubs.video
            ExternalLinkType.WEBSITE -> serverLinkInfo.thumbnail_url ?: PostStubs.website
            ExternalLinkType.IMAGE -> sourceUrl
        }

        return ExternalLinkInfo(
            type,
            serverLinkInfo.description ?: serverLinkInfo.title ?: sourceUrl,
            Uri.parse(thumbnailUrl),
            Uri.parse(sourceUrl))
    }

    private fun extractTags(content: List<ControlMetadata>, adultOnly: Boolean): Set<String> {
        val tags = content
            .asSequence()
            .filterIsInstance<ParagraphMetadata>()
            .map { it.spans }
            .flatten()
            .filterIsInstance<TagSpanInfo>()
            .map { it.value }
            .toMutableSet()

        if(adultOnly) {
            tags.add("nsfw")
        }

        return tags
    }
}