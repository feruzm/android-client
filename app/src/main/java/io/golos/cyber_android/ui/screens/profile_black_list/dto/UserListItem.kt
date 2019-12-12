package io.golos.cyber_android.ui.screens.profile_black_list.dto

import io.golos.cyber_android.ui.common.recycler_view.versioned.VersionedListItem
import io.golos.domain.dto.UserDomain

data class UserListItem(
    override val id: Long,
    override val version: Long,
    val user: UserDomain,

    /**
     * In a black list (positive) / Not in a black list (negative)
     */
    val isInPositiveState: Boolean,

    val isProgress: Boolean
) : VersionedListItem