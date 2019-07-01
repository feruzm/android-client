package io.golos.domain

import io.golos.cyber4j.model.CyberName
import io.golos.domain.entities.AuthState
import io.golos.domain.requestmodel.PushNotificationsStateModel

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-04-01.
 */
interface Persister {
    fun saveAuthState(state: AuthState)
    fun getAuthState(): AuthState?

    fun saveActiveKey(activeKey: String)
    fun getActiveKey(): String?

    fun savePushNotifsSettings(forUser: CyberName, settings: PushNotificationsStateModel)
    fun getPushNotifsSettings(forUser: CyberName): PushNotificationsStateModel
}