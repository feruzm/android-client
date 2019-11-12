package io.golos.data.mappers

import io.golos.data.dto.DocumentEntity
import io.golos.domain.dto.PostDomain

class ContentBodyEntityListToContentBodyDomainListMapper : Function1<List<DocumentEntity.ContentEntity.ContentBodyEntity>, List<PostDomain.DocumentDomain.ContentDomain.ContentBodyDomain>> {

    override fun invoke(contentBodyEntityList: List<DocumentEntity.ContentEntity.ContentBodyEntity>): List<PostDomain.DocumentDomain.ContentDomain.ContentBodyDomain> {
        return contentBodyEntityList.map {
            ContentBodyEntityToContentBodyDomainMapper().invoke(it)
        }
    }
}