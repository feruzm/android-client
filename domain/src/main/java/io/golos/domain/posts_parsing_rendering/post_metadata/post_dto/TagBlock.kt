package io.golos.domain.posts_parsing_rendering.post_metadata.post_dto

data class TagBlock(
    val id: Long?,
    var content: String
) : ParagraphItemBlock