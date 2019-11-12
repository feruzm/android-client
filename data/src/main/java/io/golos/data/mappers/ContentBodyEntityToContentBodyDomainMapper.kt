package io.golos.data.mappers

import io.golos.data.dto.DocumentEntity
import io.golos.domain.dto.PostDomain

class ContentBodyEntityToContentBodyDomainMapper : Function1<DocumentEntity.ContentEntity.ContentBodyEntity, PostDomain.DocumentDomain.ContentDomain.ContentBodyDomain> {

    override fun invoke(contentBodyEntity: DocumentEntity.ContentEntity.ContentBodyEntity): PostDomain.DocumentDomain.ContentDomain.ContentBodyDomain {

        return PostDomain.DocumentDomain.ContentDomain.ContentBodyDomain(contentBodyEntity.content,
            contentBodyEntity.id,
            contentBodyEntity.type)
    }
}