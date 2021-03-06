package io.golos.cyber_android.ui.screens.profile_black_list.view.list.users

import android.view.View
import android.view.ViewGroup
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.screens.profile_black_list.dto.UserListItem
import io.golos.cyber_android.ui.screens.profile_black_list.view.list.BlackListListItemEventsProcessor
import io.golos.cyber_android.ui.shared.glide.loadAvatar
import io.golos.cyber_android.ui.shared.recycler_view.ViewHolderBase
import io.golos.cyber_android.ui.shared.recycler_view.versioned.VersionedListItem
import kotlinx.android.synthetic.main.view_profile_black_list_list_item.view.*

class UserListItemViewHolder(
    parentView: ViewGroup
) : ViewHolderBase<BlackListListItemEventsProcessor, VersionedListItem>(
    parentView,
    R.layout.view_profile_black_list_list_item
) {

    override fun init(listItem: VersionedListItem, listItemEventsProcessor: BlackListListItemEventsProcessor) {
        if(listItem !is UserListItem) {
            return
        }

        with(listItem) {
            itemView.title.text = user.userName

            if(isInPositiveState) {
                itemView.hideButton.text = itemView.context.resources.getString(R.string.unblock)
            } else {
                itemView.hideButton.text = itemView.context.resources.getString(R.string.block)
            }

            itemView.hideButton.setOnClickListener { listItemEventsProcessor.onHideUserClick(user.userId) }

            itemView.avatar.loadAvatar(user.userAvatar)

            itemView.itemsSeparator.visibility = if(listItem.isLastItem) View.GONE else View.VISIBLE

            itemView.setOnClickListener { listItemEventsProcessor.onUserCLick(listItem.user.userId) }
        }
    }

    override fun release() {
        itemView.hideButton.setOnClickListener(null)
    }
}