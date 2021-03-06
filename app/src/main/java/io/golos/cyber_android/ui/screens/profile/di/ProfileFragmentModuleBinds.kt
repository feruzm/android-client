package io.golos.cyber_android.ui.screens.profile.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.golos.cyber_android.ui.screens.profile.model.ProfileModel
import io.golos.cyber_android.ui.screens.profile.model.ProfileModelImpl
import io.golos.cyber_android.ui.screens.profile.model.logout.LogoutUseCase
import io.golos.cyber_android.ui.screens.profile.model.logout.LogoutUseCaseImpl
import io.golos.cyber_android.ui.screens.profile.model.notifications_settings.NotificationsSettingsFacade
import io.golos.cyber_android.ui.screens.profile.model.notifications_settings.NotificationsSettingsFacadeImpl
import io.golos.cyber_android.ui.screens.profile.view_model.ProfileViewModel
import io.golos.cyber_android.ui.shared.mvvm.viewModel.FragmentViewModelFactory
import io.golos.cyber_android.ui.shared.mvvm.viewModel.FragmentViewModelFactoryImpl
import io.golos.cyber_android.ui.shared.mvvm.viewModel.ViewModelKey

@Module
abstract class ProfileFragmentModuleBinds {
    @Binds
    abstract fun provideViewModelFactory(factory: FragmentViewModelFactoryImpl): FragmentViewModelFactory

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun provideProfileViewModel(viewModel: ProfileViewModel): ViewModel

    @Binds
    abstract fun provideProfileModel(model: ProfileModelImpl): ProfileModel

    @Binds
    abstract fun provideLogoutUseCase(useCase: LogoutUseCaseImpl): LogoutUseCase

    @Binds
    abstract fun provideNotificationsSettingsFacade(facade: NotificationsSettingsFacadeImpl): NotificationsSettingsFacade
}