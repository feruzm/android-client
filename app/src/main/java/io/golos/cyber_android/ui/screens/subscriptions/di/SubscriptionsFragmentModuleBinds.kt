package io.golos.cyber_android.ui.screens.subscriptions.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.golos.cyber_android.ui.screens.subscriptions.SubscriptionsModel
import io.golos.cyber_android.ui.screens.subscriptions.SubscriptionsModelImpl
import io.golos.cyber_android.ui.screens.subscriptions.SubscriptionsViewModel
import io.golos.cyber_android.ui.shared.mvvm.viewModel.FragmentViewModelFactory
import io.golos.cyber_android.ui.shared.mvvm.viewModel.FragmentViewModelFactoryImpl
import io.golos.cyber_android.ui.shared.mvvm.viewModel.ViewModelKey
import io.golos.domain.dependency_injection.scopes.FragmentScope
import io.golos.domain.use_cases.community.*

@Module
interface SubscriptionsFragmentModuleBinds {

    @Binds
    @ViewModelKey(SubscriptionsViewModel::class)
    @IntoMap
    fun bindViewModel(viewModel: SubscriptionsViewModel): ViewModel

    @Binds
    @FragmentScope
    fun bindViewModelFactory(factory: FragmentViewModelFactoryImpl): FragmentViewModelFactory

    @Binds
    fun bindModel(model: SubscriptionsModelImpl): SubscriptionsModel

    @Binds
    fun bindGetCommunitiesUseCase(model: GetCommunitiesUseCaseImpl): GetCommunitiesUseCase

    @Binds
    fun bindGetRecommendedCommunitiesUseCase(model: GetRecommendedCommunitiesUseCaseImpl): GetRecommendedCommunitiesUseCase

    @Binds
    fun bindSubscribeToCommunityUseCase(model: SubscribeToCommunityUseCaseImpl): SubscribeToCommunityUseCase

    @Binds
    fun bindUnsubscribeToCommunityUseCase(model: UnsubscribeToCommunityUseCaseImpl): UnsubscribeToCommunityUseCase
}