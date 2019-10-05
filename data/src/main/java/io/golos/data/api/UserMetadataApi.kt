package io.golos.data.api

import io.golos.commun4j.abi.implementation.comn.social.PinComnSocialStruct
import io.golos.commun4j.abi.implementation.comn.social.UpdatemetaComnSocialStruct
import io.golos.commun4j.http.rpc.model.transaction.response.TransactionCommitted
import io.golos.commun4j.services.model.GetProfileResult
import io.golos.commun4j.sharedmodel.CyberName
import io.golos.commun4j.utils.Pair as CommunPair

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-04-30.
 */
interface UserMetadataApi {
    fun setUserMetadata(
        about: String? = null,
        coverImage: String? = null,
        profileImage: String? = null
    ): TransactionCommitted<UpdatemetaComnSocialStruct>

    fun getUserMetadata(user: CyberName): GetProfileResult

    fun pin(user: CyberName): CommunPair<TransactionCommitted<PinComnSocialStruct>, PinComnSocialStruct>

    fun unPin(user: CyberName): CommunPair<TransactionCommitted<PinComnSocialStruct>, PinComnSocialStruct>
}