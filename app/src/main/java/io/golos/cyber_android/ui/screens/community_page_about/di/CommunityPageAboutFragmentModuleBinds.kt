package io.golos.cyber_android.ui.screens.community_page_about.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.golos.cyber_android.ui.screens.community_page_about.CommunityPageAboutModel
import io.golos.cyber_android.ui.screens.community_page_about.CommunityPageAboutModelImpl
import io.golos.cyber_android.ui.screens.community_page_about.CommunityPageAboutViewModel
import io.golos.cyber_android.ui.shared.mvvm.viewModel.FragmentViewModelFactory
import io.golos.cyber_android.ui.shared.mvvm.viewModel.FragmentViewModelFactoryImpl
import io.golos.cyber_android.ui.shared.mvvm.viewModel.ViewModelKey
import io.golos.domain.dependency_injection.scopes.FragmentScope

@Module
interface CommunityPageAboutFragmentModuleBinds {

    @Binds
    @ViewModelKey(CommunityPageAboutViewModel::class)
    @IntoMap
    fun bindViewModel(viewModel: CommunityPageAboutViewModel): ViewModel

    @Binds
    @FragmentScope
    fun bindViewModelFactory(factory: FragmentViewModelFactoryImpl): FragmentViewModelFactory

    @Binds
    fun bindModel(model: CommunityPageAboutModelImpl): CommunityPageAboutModel
}