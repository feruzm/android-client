package io.golos.cyber_android.ui.screens.my_feed.view.items

import android.content.Context
import io.golos.cyber_android.ui.shared_fragments.post.view.list.view_holders.post_body.widgets.EmbedWebsiteWidget
import io.golos.domain.use_cases.post.post_dto.WebsiteBlock

class WebSitePostItem(websiteBlock: WebsiteBlock) :
    BasePostBlockItem<WebsiteBlock, EmbedWebsiteWidget>(websiteBlock) {

    override fun createWidgetView(context: Context): EmbedWebsiteWidget = EmbedWebsiteWidget(context)
}