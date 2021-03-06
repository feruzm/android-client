package io.golos.cyber_android.ui.screens.profile_communities.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.golos.cyber_android.ui.screens.profile_communities.model.ProfileCommunitiesModel
import io.golos.cyber_android.ui.screens.profile_communities.model.ProfileCommunitiesModelImpl
import io.golos.cyber_android.ui.screens.profile_communities.view_model.ProfileCommunitiesViewModel
import io.golos.cyber_android.ui.shared.mvvm.viewModel.ViewModelKey

@Module
abstract class ProfileCommunitiesFragmentModuleBinds {
    @Binds
    @IntoMap
    @ViewModelKey(ProfileCommunitiesViewModel::class)
    abstract fun provideProfileBioViewModel(viewModel: ProfileCommunitiesViewModel): ViewModel

    @Binds
    abstract fun provideProfileCommunitiesModel(model: ProfileCommunitiesModelImpl): ProfileCommunitiesModel
}