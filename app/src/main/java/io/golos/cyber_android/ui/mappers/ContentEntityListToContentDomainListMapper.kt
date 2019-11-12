package io.golos.cyber_android.ui.mappers

import io.golos.data.dto.DocumentEntity
import io.golos.domain.dto.PostDomain

class ContentEntityListToContentDomainListMapper : Function1<List<DocumentEntity.ContentEntity>, List<PostDomain.DocumentDomain.ContentDomain>> {

    override fun invoke(contentEntityList: List<DocumentEntity.ContentEntity>): List<PostDomain.DocumentDomain.ContentDomain> {
        return contentEntityList.map {
            ContentDomainToContentMapper().invoke(it)
        }
    }
}