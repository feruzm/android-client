package io.golos.cyber_android.ui.screens.community_page_leaders_list.view.list.view_holders

import android.view.ViewGroup
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.screens.community_page_leaders_list.view.list.LeadsListItemEventsProcessor
import io.golos.cyber_android.ui.shared.recycler_view.ViewHolderBase
import io.golos.cyber_android.ui.shared.recycler_view.versioned.VersionedListItem

class EmptyListItemViewHolder(
    parentView: ViewGroup
) : ViewHolderBase<LeadsListItemEventsProcessor, VersionedListItem>(
    parentView,
    R.layout.view_leaders_list_empty_item
) {
    override fun init(listItem: VersionedListItem, listItemEventsProcessor: LeadsListItemEventsProcessor) {
        // do nothing
    }

    override fun release() {
        // do nothing
    }
}
