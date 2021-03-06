package io.golos.data.mappers

import io.golos.commun4j.services.model.BlacklistCommunityItem
import io.golos.domain.dto.CommunityDomain
import io.golos.domain.dto.CommunityIdDomain

fun BlacklistCommunityItem.mapToCommunityDomain(): CommunityDomain =
    CommunityDomain(
        communityId = CommunityIdDomain(communityId),
        alias = alias,
        name = name ?: "",
        
        avatarUrl = avatarUrl,
        coverUrl = null,

        subscribersCount = 0,
        postsCount = 0,
        isSubscribed = isSubscribed ?: false
    )