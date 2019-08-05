package io.golos.cyber_android.ui.screens.login_activity.signup.fragments.name

import io.golos.cyber_android.ui.screens.login_activity.signup.SignUpScreenViewModelBase
import javax.inject.Inject

class SignUpNameViewModel
@Inject
constructor() : SignUpScreenViewModelBase() {

    companion object {
        /**
         * Exact length of the username that is valid
         */
        const val MIN_USERNAME_LENGTH = 1
        const val MAX_USERNAME_LENGTH = 32
    }

    override fun validate(field: String): Boolean {
        return field.length in (MIN_USERNAME_LENGTH .. MAX_USERNAME_LENGTH)
    }
}