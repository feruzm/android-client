package io.golos.cyber_android.ui.screens.profile_black_list.view.list.communities

import android.view.ViewGroup
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.common.glide.loadCommunity
import io.golos.cyber_android.ui.common.recycler_view.ViewHolderBase
import io.golos.cyber_android.ui.common.recycler_view.versioned.VersionedListItem
import io.golos.cyber_android.ui.common.recycler_view.versioned.CommunityListItem
import io.golos.cyber_android.ui.screens.profile_black_list.view.list.BlackListListItemEventsProcessor
import kotlinx.android.synthetic.main.view_profile_black_list_list_item.view.*

class CommunityListItemViewHolder(
    parentView: ViewGroup
) : ViewHolderBase<BlackListListItemEventsProcessor, VersionedListItem>(
    parentView,
    R.layout.view_profile_black_list_list_item
) {

    override fun init(listItem: VersionedListItem, listItemEventsProcessor: BlackListListItemEventsProcessor) {
        if(listItem !is CommunityListItem) {
            return
        }

        with(listItem) {
            itemView.title.text = community.name

            if(isInPositiveState) {
                itemView.hideButton.text = itemView.context.resources.getString(R.string.unblock)
            } else {
                itemView.hideButton.text = itemView.context.resources.getString(R.string.block)
            }

            itemView.hideButton.setOnClickListener { listItemEventsProcessor.onHideCommunityClick(community.communityId) }

            itemView.avatar.loadCommunity(community.avatarUrl)
        }
    }

    override fun release() {
        itemView.hideButton.setOnClickListener(null)
    }
}