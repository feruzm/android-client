package io.golos.domain

import io.golos.cyber4j.sharedmodel.CyberName
import io.golos.domain.entities.AppUnlockWay
import io.golos.domain.entities.AuthState
import io.golos.domain.entities.UserKeyType
import io.golos.domain.requestmodel.PushNotificationsStateModel

interface KeyValueStorageFacade {
    /**
     * Save encrypted key for AES encoding (key is encrypted via RSA alg, it works for old devices only - prior to 23 API)
     */
    fun saveAESCryptoKey(key: ByteArray)

    /**
     * Get encrypted key for AES encoding (key is encrypted via RSA alg, it works for old devices only - prior to 23 API)
     */
    fun getAESCryptoKey(): ByteArray?

    fun saveAuthState(state: AuthState)
    fun getAuthState(): AuthState?
    fun removeAuthState()

    fun saveUserKey(key: ByteArray, keyType: UserKeyType)
    fun getUserKey(keyType: UserKeyType): ByteArray?
    fun removeUserKey(keyType: UserKeyType)

    fun savePushNotificationsSettings(forUser: CyberName, settings: PushNotificationsStateModel)
    fun getPushNotificationsSettings(forUser: CyberName): PushNotificationsStateModel
    fun removePushNotificationsSettings(forUser: CyberName)

    fun savePinCode(pinCode: ByteArray)
    fun getPinCode(): ByteArray?
    fun removePinCode()

    fun saveAppUnlockWay(unlockWay: AppUnlockWay)
    fun getAppUnlockWay(): AppUnlockWay?
    fun removeAppUnlockWay()
}