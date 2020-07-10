package io.golos.cyber_android.ui.screens.post_view.view_model

import io.golos.cyber_android.ui.dto.DonateType
import io.golos.cyber_android.ui.dto.Post
import io.golos.cyber_android.ui.screens.feed_my.view_model.PostShareListener
import io.golos.cyber_android.ui.shared.widgets.post_comments.*
import io.golos.domain.dto.CommunityIdDomain
import io.golos.domain.dto.ContentIdDomain
import io.golos.domain.dto.DonationsDomain
import io.golos.domain.dto.UserBriefDomain
import io.golos.domain.use_cases.model.DiscussionIdModel

/**
 * Processing post items clicks
 */
interface PostPageViewModelListEventsProcessor :
    AttachmentWidgetListener,
    EmbedImageWidgetListener,
    EmbedWebsiteWidgetListener,
    EmbedVideoWidgetListener,
    ParagraphWidgetListener,
    PostCommentVoteListener,
    PostVoteListener,
    CommentsListener,
    RichWidgetListener,
    EmbedWidgetListener,
    PostShareListener

interface PostCommentVoteListener : BasePostBlockWidgetListener {

    fun onCommentUpVoteClick(commentId: ContentIdDomain)

    fun onCommentDownVoteClick(commentId: ContentIdDomain)
}

interface PostVoteListener : BasePostBlockWidgetListener {
    fun onUpVoteClick()

    fun onDownVoteClick()

    fun onDonateClick(donate: DonateType, contentId: ContentIdDomain, communityId: CommunityIdDomain, contentAuthor: UserBriefDomain)

    fun onDonatePopupClick(donates: DonationsDomain)
}

interface CommentsListener : BasePostBlockWidgetListener {
    fun onCommentsTitleMenuClick()

    fun onNextCommentsPageReached()

    fun onRetryLoadingFirstLevelCommentButtonClick()

    fun onCollapsedCommentsClick(parentCommentId: ContentIdDomain)

    fun onRetryLoadingSecondLevelCommentButtonClick(parentCommentId: ContentIdDomain)

    fun onCommentLongClick(commentId: ContentIdDomain)

    fun startReplyToComment(commentToReplyId: ContentIdDomain)

    fun onUserClicked(userId: String)
}