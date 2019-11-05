package io.golos.cyber_android.application.dependency_injection.graph.app.ui.main_activity.trending_feed

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.golos.cyber_android.ui.common.mvvm.viewModel.FragmentViewModelFactory
import io.golos.cyber_android.ui.common.mvvm.viewModel.FragmentViewModelFactoryImpl
import io.golos.cyber_android.ui.common.mvvm.viewModel.ViewModelKey
import io.golos.cyber_android.ui.screens.feed.community.CommunityFeedViewModel
import io.golos.domain.dependency_injection.scopes.FragmentScope
import io.golos.domain.entities.PostEntity
import io.golos.domain.interactors.feed.AbstractFeedUseCase
import io.golos.domain.interactors.feed.CommunityFeedUseCase
import io.golos.domain.interactors.model.PostModel
import io.golos.domain.interactors.user.UserMetadataUseCase
import io.golos.domain.interactors.user.UserMetadataUseCaseImpl
import io.golos.domain.requestmodel.PostFeedUpdateRequest

@Module
abstract class TrendingFeedFragmentModuleBinds {
    @Binds
    abstract fun provideCommunityFeedUseCase(useCase: CommunityFeedUseCase): AbstractFeedUseCase<PostFeedUpdateRequest, PostEntity, PostModel>

    @Binds
    @FragmentScope
    abstract fun bindViewModelFactory(factory: FragmentViewModelFactoryImpl): FragmentViewModelFactory

    @Binds
    abstract fun provideUserMetadataUseCase(useCase: UserMetadataUseCaseImpl): UserMetadataUseCase

    @Binds
    @IntoMap
    @ViewModelKey(CommunityFeedViewModel::class)
    internal abstract fun provideCommunityFeedViewModel(viewModel: CommunityFeedViewModel): ViewModel
}