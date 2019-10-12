package io.golos.data.api.vote

import io.golos.commun4j.abi.implementation.comn.gallery.VoteComnGalleryStruct
import io.golos.commun4j.http.rpc.model.transaction.response.TransactionCommitted
import io.golos.commun4j.sharedmodel.CyberName
import io.golos.domain.commun_entities.Permlink

interface VoteApi {
    fun vote(postOrCommentAuthor: CyberName, postOrCommentPermlink: Permlink, voteStrength: Short): TransactionCommitted<VoteComnGalleryStruct>
}