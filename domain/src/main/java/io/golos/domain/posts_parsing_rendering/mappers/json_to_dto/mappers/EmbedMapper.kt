package io.golos.domain.posts_parsing_rendering.mappers.json_to_dto.mappers

import io.golos.domain.posts_parsing_rendering.Attribute
import io.golos.domain.posts_parsing_rendering.CommonType
import io.golos.domain.posts_parsing_rendering.post_metadata.post_dto.EmbedBlock
import org.json.JSONObject

class EmbedMapper(
    mappersFactory: MappersFactory
) : MapperBase<EmbedBlock>(mappersFactory) {

    override fun map(source: JSONObject): EmbedBlock {
        val attributes = source.getAttributes()

        return EmbedBlock(
            source.tryLong(CommonType.ID),
            source.getContentAsUri(),
            attributes?.tryString(Attribute.TITLE),
            attributes?.tryUri(Attribute.URL),
            attributes?.tryString(Attribute.AUTHOR),
            attributes?.tryUri(Attribute.AUTHOR_URL),
            attributes?.tryString(Attribute.PROVIDER_NAME),
            attributes?.tryString(Attribute.DESCRIPTION),
            attributes?.tryUri(Attribute.THUMBNAIL_URL),
            attributes?.tryInt(Attribute.HEIGHT) ?: attributes?.tryInt(Attribute.THUMBNAIL_WIDTH),
            attributes?.tryInt(Attribute.HEIGHT) ?: attributes?.tryInt(Attribute.THUMBNAIL_HEIGHT),
            attributes?.tryString(Attribute.HTML)
        )
    }
}