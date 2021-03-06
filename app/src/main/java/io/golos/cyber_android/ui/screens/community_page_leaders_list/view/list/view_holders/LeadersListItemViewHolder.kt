package io.golos.cyber_android.ui.screens.community_page_leaders_list.view.list.view_holders

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.screens.community_page_leaders_list.dto.LeaderListItem
import io.golos.cyber_android.ui.screens.community_page_leaders_list.view.list.LeadsListItemEventsProcessor
import io.golos.cyber_android.ui.shared.characters.SpecialChars
import io.golos.cyber_android.ui.shared.glide.loadLeader
import io.golos.cyber_android.ui.shared.recycler_view.ViewHolderBase
import io.golos.cyber_android.ui.shared.recycler_view.versioned.VersionedListItem
import io.golos.utils.format.PercentFormatter
import io.golos.utils.format.size.PluralSizeFormatter
import io.golos.utils.getColorRes
import io.golos.utils.helpers.appendSpannedText
import kotlinx.android.synthetic.main.view_leaders_list_item.view.*

class LeadersListItemViewHolder(
    parentView: ViewGroup
) : ViewHolderBase<LeadsListItemEventsProcessor, VersionedListItem>(
    parentView,
    R.layout.view_leaders_list_item
) {
    private val pointsFormatter = PluralSizeFormatter(
        parentView.context.applicationContext,
        R.plurals.formatter_points_formatted
    )

    override fun init(listItem: VersionedListItem, listItemEventsProcessor: LeadsListItemEventsProcessor) {
        if(listItem !is LeaderListItem) {
            return
        }

        with(itemView) {
            ivLogo.loadLeader(listItem.avatarUrl, listItem.ratingPercent.toFloat())
            ivLogo.setOnClickListener { listItemEventsProcessor.onUserClick(listItem.userId) }

            leaderName.text = listItem.username
            leaderName.setOnClickListener { listItemEventsProcessor.onUserClick(listItem.userId) }

            leaderPoints.text = getPointsText(context, listItem)

            voteButton.visibility = if(listItem.isVoted) View.INVISIBLE else View.VISIBLE
            votedButton.visibility = if(listItem.isVoted) View.VISIBLE else View.INVISIBLE

            voteButton.setOnClickListener { listItemEventsProcessor.vote(listItem.userId) }
            votedButton.setOnClickListener { listItemEventsProcessor.unvote(listItem.userId) }
        }
    }

    override fun release() {
        itemView.ivLogo.setOnClickListener(null)
        itemView.leaderName.setOnClickListener(null)
        itemView.voteButton.setOnClickListener(null)
        itemView.votedButton.setOnClickListener(null)
    }

    private fun getPointsText(context: Context, listItem: LeaderListItem) : SpannableStringBuilder {
        val result = SpannableStringBuilder()

        with(listItem) {
            result.append( pointsFormatter.format(rating.toInt()))

            result.append(" ")
            result.append(" ${SpecialChars.BULLET} ")
            result.append(" ")

            result.appendSpannedText(
                PercentFormatter.format(ratingPercent),
                ForegroundColorSpan(context.resources.getColorRes(R.color.blue)))
        }

        return result
    }
}