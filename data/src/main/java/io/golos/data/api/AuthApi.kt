package io.golos.data.api

import io.golos.commun4j.model.UserProfile
import io.golos.commun4j.services.model.AuthResult
import io.golos.commun4j.services.model.ResolvedProfile
import io.golos.commun4j.sharedmodel.AuthSecret
import io.golos.commun4j.sharedmodel.CyberName


/**
 * Created by yuri yurivladdurain@gmail.com on 2019-03-20.
 */
interface AuthApi {
    fun setActiveUserCreds(user: CyberName, activeKey: String)

    fun getUserAccount(user: CyberName): UserProfile

    fun getAuthSecret(): AuthSecret

    fun authWithSecret(user: String,
                       secret: String,
                       signedSecret: String): AuthResult

    fun resolveCanonicalCyberName(name: String): ResolvedProfile
}
