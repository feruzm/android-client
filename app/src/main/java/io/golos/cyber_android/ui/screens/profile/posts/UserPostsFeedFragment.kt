package io.golos.cyber_android.ui.screens.profile.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import io.golos.cyber_android.R
import io.golos.cyber_android.application.App
import io.golos.cyber_android.application.dependency_injection.graph.app.ui.main_activity.user_posts_feed.UserPostsFeedFragmentComponent
import io.golos.cyber_android.ui.Tags
import io.golos.cyber_android.ui.common.mvvm.viewModel.FragmentViewModelFactory
import io.golos.cyber_android.ui.common.posts.AbstractFeedFragment
import io.golos.cyber_android.ui.common.posts.PostsAdapter
import io.golos.cyber_android.ui.dialogs.sort.SortingTypeDialogFragment
import io.golos.cyber_android.ui.screens.main_activity.feed.*
import io.golos.cyber_android.ui.shared_fragments.post.PostActivity
import io.golos.cyber_android.ui.shared_fragments.post.PostPageFragment
import io.golos.cyber_android.ui.screens.profile.ProfileActivity
import io.golos.cyber_android.utils.asEvent
import io.golos.cyber_android.views.utils.TopDividerItemDecoration
import io.golos.cyber_android.ui.common.widgets.sorting.SortingType
import io.golos.cyber_android.ui.common.widgets.sorting.SortingWidget
import io.golos.cyber_android.ui.common.widgets.sorting.TimeFilter
import io.golos.cyber_android.ui.common.widgets.sorting.TrendingSort
import io.golos.domain.entities.CyberUser
import io.golos.domain.entities.PostEntity
import io.golos.domain.interactors.model.PostModel
import io.golos.domain.requestmodel.PostFeedUpdateRequest
import io.golos.domain.requestmodel.QueryResult
import kotlinx.android.synthetic.main.fragment_user_posts_feed_list.*
import javax.inject.Inject

/**
 * Fragment that represents POSTS tab of the Profile Page
 */
open class UserPostsFeedFragment :
    AbstractFeedFragment<PostFeedUpdateRequest, PostEntity, PostModel, FeedPageTabViewModel<PostFeedUpdateRequest>>() {

    override lateinit var viewModel: FeedPageTabViewModel<PostFeedUpdateRequest>

    @Inject
    internal lateinit var viewModelFactory: FragmentViewModelFactory

    override val feedList: RecyclerView
        get() = feedRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.injections.get<UserPostsFeedFragmentComponent>(CyberUser(arguments?.getString(Tags.USER_ID)!!)).inject(this)
    }

    override fun onDestroy() {
        App.injections.release<UserPostsFeedFragmentComponent>()
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_user_posts_feed_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        swipeRefresh.setOnRefreshListener {
            viewModel.requestRefresh()
        }
        swipeRefresh.isEnabled = false
        setupSortingWidget()
    }

    override fun onNewData(data: List<PostModel>) {
        swipeRefresh.isRefreshing = false
        if (data.isEmpty()) {
            empty.visibility = View.VISIBLE
            feedList.visibility = View.GONE
        } else {
            empty.visibility = View.GONE
            feedList.visibility = View.VISIBLE
        }
    }

    override fun setupFeedAdapter() {
        feedList.adapter = HeadersPostsAdapter(
            object : PostsAdapter.Listener {
                override fun onUpvoteClick(post: PostModel) {
                    viewModel.onUpvote(post)
                }

                override fun onDownvoteClick(post: PostModel) {
                    viewModel.onDownvote(post)
                }

                override fun onPostClick(post: PostModel) {
                    startActivity(PostActivity.getIntent(requireContext(), PostPageFragment.Args(post.contentId)))
                }

                override fun onSendClick(post: PostModel, comment: CharSequence) {
                    viewModel.sendComment(post, comment)
                }

                override fun onPostCommentsClick(post: PostModel) {
                    startActivity(
                        PostActivity.getIntent(
                            requireContext(),
                            PostPageFragment.Args(post.contentId, true)
                        )
                    )
                }

                override fun onPostShare(post: PostModel) {

                }

                override fun onAuthorClick(post: PostModel) {
                    startActivity(ProfileActivity.getIntent(requireContext(), post.author.userId.userId))
                }

                override fun onPostMenuClick(postModel: PostModel) {
                    showDiscussionMenu(postModel)
                }
            },
            isEditorWidgetSupported = false,
            isSortingWidgetSupported = true
        )
        feedList.addItemDecoration(TopDividerItemDecoration(requireContext()))
    }

    override fun setupEventsProvider() {
        (targetFragment as? FeedPageLiveDataProvider)
            ?.provideEventsLiveData()?.asEvent()?.observe(this, Observer { event ->
                event.getIfNotHandled()?.let {
                    when (it) {
                        is FeedPageViewModel.Event.RefreshRequestEvent -> viewModel.requestRefresh()
                    }
                }
            })

        viewModel.loadingStatusLiveData.observe(this, Observer { isLoading ->
            if (!isLoading)
                swipeRefresh.isRefreshing = false
        })

        viewModel.discussionCreationLiveData.observe(this, Observer {
            it.getIfNotHandled()?.let { result ->
                when (result) {
                    is QueryResult.Loading -> showLoading()
                    is QueryResult.Success -> hideLoading()
                    is QueryResult.Error -> {
                        hideLoading()
                        showDiscussionCreationError(result.error)
                    }
                }
            }
        })
    }

    override fun setupWidgetsLiveData() {
        viewModel.getSortingWidgetStateLiveData.observe(this, Observer { state ->
            (feedList.adapter as HeadersPostsAdapter).apply {
                sortingWidgetState = state
            }
        })
    }

    private fun setupSortingWidget() {
        (feedList.adapter as HeadersPostsAdapter).sortingWidgetListener = object : SortingWidget.Listener {
            override fun onTrendingSortClick() {
                showSortingDialog(arrayOf(TrendingSort.NEW, TrendingSort.TOP))
            }

            override fun onTimeFilterClick() {
                showSortingDialog(
                    arrayOf(
                        TimeFilter.PAST_24_HR,
                        TimeFilter.PAST_WEEK,
                        TimeFilter.PAST_MONTH,
                        TimeFilter.PAST_YEAR,
                        TimeFilter.OF_ALL_TIME
                    )
                )
            }
        }
    }

    private fun showSortingDialog(values: Array<SortingType>) {
        SortingTypeDialogFragment
            .newInstance(values)
            .apply {
                setTargetFragment(this@UserPostsFeedFragment, SORT_REQUEST_CODE)
            }
            .show(requireFragmentManager(), null)
    }

    override fun setupViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UserPostsFeedViewModel::class.java)
    }

    companion object {
        fun newInstance(userId: String) =
            UserPostsFeedFragment().apply {
                arguments = Bundle().apply {
                    putString(Tags.USER_ID, userId)
                }
            }
    }
}