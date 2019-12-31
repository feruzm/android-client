package io.golos.cyber_android.application.dependency_injection.graph.app.ui.bio_fragment

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.golos.cyber_android.ui.common.mvvm.viewModel.FragmentViewModelFactory
import io.golos.cyber_android.ui.common.mvvm.viewModel.FragmentViewModelFactoryImpl
import io.golos.cyber_android.ui.common.mvvm.viewModel.ViewModelKey
import io.golos.cyber_android.ui.screens.login_sign_up_bio.OnboardingBioViewModel
import io.golos.domain.use_cases.user.UserMetadataUseCase
import io.golos.domain.use_cases.user.UserMetadataUseCaseImpl

@Module
abstract class BioFragmentModuleBinds {
    @Binds
    @IntoMap
    @ViewModelKey(OnboardingBioViewModel::class)
    abstract fun provideEditProfileBioViewModel(viewModel: OnboardingBioViewModel): ViewModel

    @Binds
    abstract fun provideUserMetadataUseCase(useCase: UserMetadataUseCaseImpl): UserMetadataUseCase

    @Binds
    abstract fun bindViewModelFactory(factory: FragmentViewModelFactoryImpl): FragmentViewModelFactory
}