package io.golos.cyber_android.application.dependency_injection.graph.app.ui.post_page_fragment

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.golos.cyber_android.ui.common.mvvm.viewModel.FragmentViewModelFactory
import io.golos.cyber_android.ui.common.mvvm.viewModel.FragmentViewModelFactoryImpl
import io.golos.cyber_android.ui.common.mvvm.viewModel.ViewModelKey
import io.golos.cyber_android.ui.screens.post_view.helpers.CommentTextRenderer
import io.golos.cyber_android.ui.screens.post_view.helpers.CommentTextRendererImpl
import io.golos.cyber_android.ui.screens.post_view.model.PostPageModel
import io.golos.cyber_android.ui.screens.post_view.model.PostPageModelImpl
import io.golos.cyber_android.ui.screens.post_view.model.comments_processing.CommentsProcessingFacade
import io.golos.cyber_android.ui.screens.post_view.model.comments_processing.CommentsProcessingFacadeImpl
import io.golos.cyber_android.ui.screens.post_view.model.comments_processing.comments_storage.CommentsStorage
import io.golos.cyber_android.ui.screens.post_view.model.comments_processing.comments_storage.CommentsStorageImpl
import io.golos.cyber_android.ui.screens.post_view.model.post_list_data_source.PostListDataSource
import io.golos.cyber_android.ui.screens.post_view.model.post_list_data_source.PostListDataSourceComments
import io.golos.cyber_android.ui.screens.post_view.model.post_list_data_source.PostListDataSourceImpl
import io.golos.cyber_android.ui.screens.post_view.model.post_list_data_source.PostListDataSourcePostControls
import io.golos.cyber_android.ui.screens.post_view.model.voting.PostVotingMachineImpl
import io.golos.cyber_android.ui.screens.post_view.model.voting.VotingMachine
import io.golos.cyber_android.ui.screens.post_view.view_model.PostPageViewModel
import io.golos.domain.dependency_injection.scopes.FragmentScope
import io.golos.domain.use_cases.community.SubscribeToCommunityUseCase
import io.golos.domain.use_cases.community.SubscribeToCommunityUseCaseImpl
import io.golos.domain.use_cases.community.UnsubscribeToCommunityUseCase
import io.golos.domain.use_cases.community.UnsubscribeToCommunityUseCaseImpl
import io.golos.domain.use_cases.feed.PostWithCommentUseCase
import io.golos.domain.use_cases.feed.PostWithCommentUseCaseImpl

@Module
abstract class PostPageFragmentModuleBinds {
    @Binds
    abstract fun providePostWithCommentsUseCase(useCase: PostWithCommentUseCaseImpl): PostWithCommentUseCase

    @Binds
    abstract fun bindViewModelFactory(factory: FragmentViewModelFactoryImpl): FragmentViewModelFactory

    @Binds
    @IntoMap
    @ViewModelKey(PostPageViewModel::class)
    internal abstract fun providePostPageViewModel(viewModel: PostPageViewModel): ViewModel

    @Binds
    abstract fun bindModel(model: PostPageModelImpl): PostPageModel

    @Binds
    @FragmentScope
    abstract fun bindPostListDataSource(dataSource: PostListDataSourceImpl): PostListDataSource

    @Binds
    @FragmentScope
    abstract fun bindPostListDataSourceForPostControls(dataSource: PostListDataSourceImpl): PostListDataSourcePostControls

    @Binds
    @FragmentScope
    abstract fun bindPostListDataSourceForComments(dataSource: PostListDataSourceImpl): PostListDataSourceComments

    @Binds
    @FragmentScope
    abstract fun bindVotingMachine(machine: PostVotingMachineImpl): VotingMachine

    @Binds
    abstract fun bindCommentsLoadingFacade(facade: CommentsProcessingFacadeImpl): CommentsProcessingFacade

    @Binds
    abstract fun bindPostedCommentsCollection(collection: CommentsStorageImpl): CommentsStorage

    @Binds
    abstract fun bindCommentTextRenderer(renderer: CommentTextRendererImpl): CommentTextRenderer

    @Binds
    abstract fun provideSubscribeToCommunityUseCase(useCase: SubscribeToCommunityUseCaseImpl): SubscribeToCommunityUseCase

    @Binds
    abstract fun provideUnsubscribeToCommunityUseCase(useCase: UnsubscribeToCommunityUseCaseImpl): UnsubscribeToCommunityUseCase
}