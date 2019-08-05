package io.golos.cyber_android.ui.screens.login_activity.signup.fragments.onboardingImage

import androidx.lifecycle.ViewModel
import io.golos.domain.interactors.user.UserMetadataUseCase
import io.golos.domain.interactors.user.UserMetadataUseCaseImpl
import javax.inject.Inject

class OnboardingUserImageViewModel
@Inject
constructor(
    private val userMetadataUseCase: UserMetadataUseCase
) : ViewModel() {

    val getUserMetadataLiveData = userMetadataUseCase.getAsLiveData

    init {
        userMetadataUseCase.subscribe()
    }

    override fun onCleared() {
        super.onCleared()
        userMetadataUseCase.unsubscribe()
    }
}
