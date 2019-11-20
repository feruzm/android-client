package io.golos.cyber_android.ui.dialogs.post.view.items

import android.view.ViewGroup
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.common.recycler_view.ViewHolderBase
import io.golos.cyber_android.ui.dialogs.post.model.FavoriteListItem
import io.golos.cyber_android.ui.dialogs.post.model.PostMenuModelListEventProcessor
import io.golos.cyber_android.utils.setDrawableToEnd
import io.golos.cyber_android.utils.setStyle
import kotlinx.android.synthetic.main.item_post_menu.view.*

class RemoveFromFavoriteItem(
    parentView: ViewGroup
) : ViewHolderBase<PostMenuModelListEventProcessor, FavoriteListItem>(
    parentView,
    R.layout.item_post_menu
) {

    override fun init(listItem: FavoriteListItem, listItemEventsProcessor: PostMenuModelListEventProcessor) {
        with(itemView) {
            menuAction.text = context.getString(R.string.favorite_remove_post)
            menuAction.setDrawableToEnd(R.drawable.ic_favorite)
            menuAction.setStyle(R.style.BottomSheetMenuItem)
            menuAction.setOnClickListener {
                listItemEventsProcessor.onRemoveFromFavoriteItemClick()
            }
        }
    }

    override fun release() {
        itemView.menuAction.setOnClickListener(null)
    }
}