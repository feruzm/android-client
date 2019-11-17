package io.golos.cyber_android.ui.screens.main_activity.feed.my_feed.view.items

import android.content.Context
import io.golos.cyber_android.ui.common.base.adapter.RecyclerItem
import io.golos.cyber_android.ui.shared_fragments.post.view.list.view_holders.post_body.widgets.AttachmentWidgetListener
import io.golos.cyber_android.ui.shared_fragments.post.view.list.view_holders.post_body.widgets.AttachmentsWidget
import io.golos.domain.use_cases.post.post_dto.AttachmentsBlock

class AttachmentPostItem(
    val attachmentsBlock: AttachmentsBlock,
    widgetListener: AttachmentWidgetListener? = null
) :
    BasePostBlockItem<AttachmentsBlock, AttachmentWidgetListener, AttachmentsWidget>(attachmentsBlock, widgetListener) {

    override fun createWidgetView(context: Context): AttachmentsWidget = AttachmentsWidget(context)

    override fun areItemsTheSame(): Int = attachmentsBlock.hashCode()

    override fun areContentsSame(item: RecyclerItem): Boolean {
        if(item is AttachmentPostItem){
            return attachmentsBlock == item.attachmentsBlock
        }
        return false
    }
}