package io.golos.cyber_android.ui.screens.myfeed

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import io.golos.cyber_android.R
import io.golos.cyber_android.application.App
import io.golos.cyber_android.application.dependency_injection.graph.app.ui.my_feed.MyFeedFragmentComponent
import io.golos.cyber_android.databinding.FragmentMyFeedBinding
import io.golos.cyber_android.ui.common.mvvm.FragmentBaseMVVM
import io.golos.cyber_android.ui.screens.postslist.GetPostsConfiguration
import io.golos.cyber_android.ui.screens.postslist.PostsListAdapter
import io.golos.cyber_android.ui.screens.postslist.PostsListViewModel
import kotlinx.android.synthetic.main.fragment_my_feed.*

class MyFeedFragment : FragmentBaseMVVM<FragmentMyFeedBinding, PostsListViewModel>() {

    override fun linkViewModel(binding: FragmentMyFeedBinding, viewModel: PostsListViewModel) {
        binding.viewModel = viewModel
    }

    override fun provideViewModelType(): Class<PostsListViewModel> = PostsListViewModel::class.java

    override fun layoutResId(): Int = R.layout.fragment_my_feed

    override fun inject() = App.injections.get<MyFeedFragmentComponent>(arguments!!.getParcelable(UPLOAD_POSTS_CONFIGURATION))
        .inject(this)

    override fun releaseInjection() {
        App.injections.release<MyFeedFragmentComponent>()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvPosts.adapter = PostsListAdapter()
    }

    companion object {

        private const val UPLOAD_POSTS_CONFIGURATION = "UPLOAD_POSTS_CONFIGURATION"

        fun newInstance(getPostsConfiguration: GetPostsConfiguration): Fragment {
            val postsListFragment = MyFeedFragment()
            postsListFragment.apply {
                arguments = bundleOf(
                    UPLOAD_POSTS_CONFIGURATION to getPostsConfiguration
                )
            }
            return postsListFragment
        }
    }
}