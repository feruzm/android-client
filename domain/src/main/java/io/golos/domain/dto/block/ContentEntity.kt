package io.golos.domain.dto.block

import com.squareup.moshi.Json

data class ContentEntity(
    @Json(name = "id") val id: Long?,
    @Json(name = "content") val content: String,
    @Json(name = "type") val type: String,
    @Json(name = "attributes") val attributes: ContentAttributeEntity? = null
)