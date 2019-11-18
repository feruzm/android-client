package io.golos.cyber_android.ui.screens.community_page.child_pages.leads_list.view.list.view_holders

import android.view.ViewGroup
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.common.recycler_view.ViewHolderBase
import io.golos.cyber_android.ui.common.recycler_view.versioned.VersionedListItem
import io.golos.cyber_android.ui.screens.community_page.child_pages.leads_list.view.list.LeadsListItemEventsProcessor

class LoadingListItemViewHolder(
    parentView: ViewGroup
) : ViewHolderBase<LeadsListItemEventsProcessor, VersionedListItem>(
    parentView,
    R.layout.view_loading_list_item
) {
    override fun init(listItem: VersionedListItem, listItemEventsProcessor: LeadsListItemEventsProcessor) {
        // do nothing
    }

    override fun release() {
        // do nothing
    }
}
