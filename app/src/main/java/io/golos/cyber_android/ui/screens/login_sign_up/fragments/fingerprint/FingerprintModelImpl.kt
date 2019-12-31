package io.golos.cyber_android.ui.screens.login_sign_up.fragments.fingerprint

import io.golos.domain.DispatchersProvider
import io.golos.data.persistence.key_value_storage.KeyValueStorageFacade
import io.golos.domain.dto.AppUnlockWay
import io.golos.domain.dto.AuthType
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class FingerprintModelImpl
@Inject
constructor(
    private val keyValueStorage: KeyValueStorageFacade,
    private val dispatchersProvider: DispatchersProvider):
    FingerprintModel {

    /**
     * @return true in case of success
     */
    override suspend fun saveAppUnlockWay(unlockWay: AppUnlockWay): Boolean =
        withContext(dispatchersProvider.ioDispatcher) {
            try {
                keyValueStorage.saveAppUnlockWay(unlockWay)

                val newAuthState = keyValueStorage.getAuthState()!!.copy(isFingerprintSettingsPassed = true)
                keyValueStorage.saveAuthState(newAuthState)

                true
            } catch(ex: Exception) {
                Timber.e(ex)
                false
            }
        }

    override suspend fun getAuthType(): AuthType =
        withContext(dispatchersProvider.ioDispatcher) {
            keyValueStorage.getAuthState()!!.type
        }
}