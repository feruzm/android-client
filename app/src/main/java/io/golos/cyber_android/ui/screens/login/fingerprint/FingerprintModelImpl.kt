package io.golos.cyber_android.ui.screens.login.fingerprint

import io.golos.domain.DispatchersProvider
import io.golos.domain.KeyValueStorageFacade
import io.golos.domain.Logger
import io.golos.domain.entities.AppUnlockWay
import kotlinx.coroutines.withContext

class FingerprintModelImpl(
    private val keyValueStorage: KeyValueStorageFacade,
    private val logger: Logger,
    private val dispatchersProvider: DispatchersProvider): FingerprintModel {
    /**
     * @return true in case of success
     */
    override suspend fun saveAppUnlockWay(unlockWay: AppUnlockWay): Boolean =
        withContext(dispatchersProvider.networkDispatcher) {
            try {
                keyValueStorage.saveAppUnlockWay(unlockWay)
                true
            } catch(ex: Exception) {
                logger(ex)
                false
            }
        }
}