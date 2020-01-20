package io.golos.domain.use_cases.post.post_dto

import android.net.Uri

data class WebsiteBlock(
    val id: Long?,
    val content: Uri,
    val thumbnailUrl: Uri?,
    val title: String?,
    val providerName: String?,
    val description: String?
): MediaBlock