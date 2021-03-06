package io.golos.cyber_android.ui.screens.post_page_menu.model

import io.golos.cyber_android.ui.shared.recycler_view.versioned.VersionedListItem
import io.golos.utils.id.IdUtil

data class ReportListItem(
    override val version: Long = 0,
    override val id: Long = IdUtil.generateLongId(),
    override val isFirstItem: Boolean = false,
    override val isLastItem: Boolean = false
) : VersionedListItem