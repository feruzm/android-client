package io.golos.domain.rules

import io.golos.domain.dto.CommentEntity
import io.golos.domain.dto.FeedEntity
import io.golos.domain.dto.PostEntity
import javax.inject.Inject

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-03-13.
 */
class EmptyPostFeedProducer
@Inject
constructor() : EmptyEntityProducer<FeedEntity<PostEntity>> {
    override fun invoke(): FeedEntity<PostEntity> {
        return FeedEntity(emptyList(), null, "")
    }
}

class EmptyCommentFeedProducer
@Inject
constructor() : EmptyEntityProducer<FeedEntity<CommentEntity>> {
    override fun invoke(): FeedEntity<CommentEntity> {
        return FeedEntity(emptyList(), null, "")
    }
}