package io.golos.cyber_android.ui.screens.post_edit.fragment.di

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.golos.cyber_android.ui.screens.post_edit.fragment.model.EditorPageModel
import io.golos.cyber_android.ui.screens.post_edit.fragment.view_model.EditorPageViewModel
import io.golos.cyber_android.ui.shared.mvvm.viewModel.ViewModelKey
import io.golos.domain.DispatchersProvider
import io.golos.domain.commun_entities.Permlink
import io.golos.domain.dto.CommentEntity
import io.golos.domain.dto.ContentIdDomain
import io.golos.domain.mappers.PostEntitiesToModelMapper
import io.golos.domain.repositories.DiscussionsFeedRepository
import io.golos.domain.requestmodel.CommentFeedUpdateRequest
import io.golos.domain.use_cases.UseCase
import io.golos.domain.use_cases.model.DiscussionIdModel
import io.golos.domain.use_cases.model.UploadedImagesModel
import io.golos.domain.use_cases.publish.EmbedsUseCase

@Module
class EditorPageFragmentModule(private val contentId: ContentIdDomain?) {

    @Provides
    internal fun provideContentId(): ContentIdDomain? = contentId

    @Provides
    internal fun provideDiscussionModelId(contentId: ContentIdDomain?): DiscussionIdModel? =
        contentId?.let { DiscussionIdModel(contentId.userId.userId, Permlink(contentId.permlink)) }

    @Provides
    @IntoMap
    @ViewModelKey(EditorPageViewModel::class)
    internal fun provideEditorPageViewModel(
        appContext: Context,
        embedsUseCase: EmbedsUseCase,
        imageUploadUseCase: UseCase<UploadedImagesModel>,
        postToEdit: DiscussionIdModel?,
        postEntityToModelMapper: PostEntitiesToModelMapper,
        commentsRepository: DiscussionsFeedRepository<CommentEntity, CommentFeedUpdateRequest>,
        dispatchersProvider: DispatchersProvider,
        model: EditorPageModel
    ): ViewModel {
        return EditorPageViewModel(
            appContext,
            dispatchersProvider,
            embedsUseCase,
            imageUploadUseCase,
            contentId,
            model
        )
    }
}