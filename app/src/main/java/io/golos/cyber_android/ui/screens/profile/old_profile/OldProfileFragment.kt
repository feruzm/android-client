package io.golos.cyber_android.ui.screens.profile.old_profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatDrawableManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import io.golos.commun4j.utils.toCyberName
import io.golos.cyber_android.R
import io.golos.cyber_android.application.App
import io.golos.cyber_android.application.dependency_injection.graph.app.ui.main_activity.profile_fragment.OldProfileFragmentComponent
import io.golos.cyber_android.ui.Tags
import io.golos.cyber_android.ui.common.base.FragmentBase
import io.golos.cyber_android.ui.common.extensions.reduceDragSensitivity
import io.golos.cyber_android.ui.common.mvvm.viewModel.FragmentViewModelFactory
import io.golos.cyber_android.ui.dialogs.ImagePickerDialog
import io.golos.cyber_android.ui.dialogs.NotificationDialog
import io.golos.cyber_android.ui.screens.edit_profile_bio_activity.EditProfileBioActivity
import io.golos.cyber_android.ui.screens.followers.FollowersFragment
import io.golos.cyber_android.ui.screens.feed.FeedPageLiveDataProvider
import io.golos.cyber_android.ui.screens.feed.FeedViewModel
import io.golos.cyber_android.ui.screens.profile.old_profile.edit.ImagePickerFragmentBase
import io.golos.cyber_android.ui.screens.profile.old_profile.edit.avatar.EditProfileAvatarActivity
import io.golos.cyber_android.ui.screens.profile.old_profile.edit.avatar.EditProfileAvatarFragment
import io.golos.cyber_android.ui.screens.profile.old_profile.edit.cover.EditProfileCoverActivity
import io.golos.cyber_android.ui.screens.profile.old_profile.edit.cover.EditProfileCoverFragment
import io.golos.cyber_android.ui.screens.profile.old_profile.edit.settings.ProfileSettingsActivity
import io.golos.cyber_android.ui.screens.profile.old_profile.posts.UserPostsFeedFragment
import io.golos.cyber_android.ui.screens.subscriptions.SubscriptionsFragment
import io.golos.cyber_android.ui.shared_fragments.bio.EditProfileBioFragment
import io.golos.cyber_android.ui.utils.asEvent
import io.golos.cyber_android.ui.utils.TabLayoutMediator
import io.golos.data.errors.AppError
import io.golos.domain.use_cases.model.UserMetadataModel
import io.golos.domain.requestmodel.QueryResult
import kotlinx.android.synthetic.main.fragment_profile.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


const val REQUEST_UPDATE_COVER_DIALOG = 101
const val REQUEST_UPDATE_PHOTO_DIALOG = 102
const val REQUEST_UPDATE_COVER = 103
const val REQUEST_UPDATE_PHOTO = 104
const val REQUEST_FEED = 105

/**
 * Fragment that represents user profile. [OldProfileViewModel] produces [OldProfileViewModel.Profile] objects, that
 * provides [UserMetadataModel] (with fields like username, avatar, stats etc) and isActiveUserProfile boolean value. This value
 * is used in this fragment to determine if this Profile Page belongs to the actual user that uses the app.
 */
class OldProfileFragment : FragmentBase(), FeedPageLiveDataProvider {

    enum class Tab(@StringRes val title: Int, val index: Int) {
        POSTS(R.string.posts, 0), COMMENTS(R.string.comments, 1)
    }

    private lateinit var viewModel: OldProfileViewModel

    @Inject
    internal lateinit var viewModelFactory: FragmentViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.injections.get<OldProfileFragmentComponent>(getUserName().toCyberName()).inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        App.injections.release<OldProfileFragmentComponent>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupViewPager()
        setupTabLayout()
        setupViewModel()
        observeViewModel()

        settings.setOnClickListener { startActivity(ProfileSettingsActivity.getIntent(requireContext())) }
        updateCover.setOnClickListener {
            ImagePickerDialog.newInstance(ImagePickerDialog.Target.COVER).apply {
                setTargetFragment(this@OldProfileFragment,
                    REQUEST_UPDATE_COVER_DIALOG
                )
            }.show(requireFragmentManager(), "cover")
        }


        updatePhoto.setOnClickListener {
            ImagePickerDialog.newInstance(ImagePickerDialog.Target.AVATAR).apply {
                setTargetFragment(this@OldProfileFragment,
                    REQUEST_UPDATE_PHOTO_DIALOG
                )
            }.show(requireFragmentManager(), "cover")
        }

        ivBack.setOnClickListener { requireActivity().finish() }
        ivBack.visibility = if (isSeparateActivity()) View.VISIBLE else View.GONE

        profileSwipeRefresh.setOnRefreshListener { viewModel.requestRefresh() }

        actionBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            profileSwipeRefresh.isEnabled = verticalOffset == 0
        })

        follow.setOnClickListener { viewModel.switchFollowingStatus() }

        followersPhotosView.visibility = View.GONE
        followingPhotosView.visibility = View.GONE
        subscribersPhotosView.visibility = View.GONE

        profilePager.reduceDragSensitivity()

        subscribers.setOnClickListener {
            val tag = SubscriptionsFragment::javaClass.name
            requireFragmentManager()
                .beginTransaction()
                .add(R.id.rootContainer, SubscriptionsFragment.newInstance(), tag)
                .addToBackStack(tag)
                .commit()
        }

        followers.setOnClickListener {
            val tag = FollowersFragment::javaClass.name
            requireFragmentManager()
                .beginTransaction()
                .add(R.id.rootContainer, FollowersFragment.newInstance(), tag)
                .addToBackStack(tag)
                .commit()
        }
    }

    /**
     * Returns true if this fragment is located on separate [ProfileActivity]. This way we need to show
     * dedicated back button
     */
    private fun isSeparateActivity() = activity is ProfileActivity

    private fun observeViewModel() {
        viewModel.getProfileLiveData.observe(this, Observer { result ->
            when (result) {
                is QueryResult.Success -> {
                    bindProfile(result.originalQuery.metadata)
                    updateControlsAccessibility(result.originalQuery.isActiveUserProfile)
                }
                is QueryResult.Error -> onError(result)
                is QueryResult.Loading -> onLoading()
            }
        })

        viewModel.getMetadataUpdateStateLiveData.asEvent().observe(this, Observer { event ->
            event.getIfNotHandled()?.let {
                when (it) {
                    is QueryResult.Success -> hideLoading()
                    is QueryResult.Error -> {
                        hideLoading()
                        val errorMsg = when (it.error) {
                            is AppError.AlreadyPinnedError -> R.string.you_already_have_pinned_this_account
                            is AppError.NotPinnedError -> R.string.you_have_not_pinned_this_account
                            is AppError.RequestTimeOutException -> R.string.request_timeout_error
                            else -> R.string.unknown_error
                        }
                        NotificationDialog.newInstance(getString(errorMsg))
                            .show(requireFragmentManager(), "error")
                    }
                    is QueryResult.Loading -> showLoading()
                }
            }
        })
    }

    private fun onLoading() {
        //Toast.makeText(requireContext(), "loading profile", Toast.LENGTH_SHORT).show()
    }

    private fun onError(result: QueryResult.Error<OldProfileViewModel.Profile>? = null) {
        when {
            //profile with this username wasn't found
            result?.originalQuery == OldProfileViewModel.Profile.NOT_FOUND && isSeparateActivity() -> {
                //and its separate activity, so it can be finished safely
                NotificationDialog.newInstance(getString(R.string.user_not_found_message))
                    .run {
                        isCancelable = false
                        listener = this@OldProfileFragment.requireActivity()::finish
                        show(this@OldProfileFragment.requireFragmentManager(), "404_not_found")
                    }
            }
            result?.originalQuery == OldProfileViewModel.Profile.NOT_FOUND && !isSeparateActivity() -> {
                //and its not separate activity, so we cannot finish it
                Toast.makeText(requireContext(), getString(R.string.user_not_found_message), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(requireContext(), "loading profile error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Binds user metadata to this profile.
     */
    private fun bindProfile(profile: UserMetadataModel) {
        updateEditBioControls(profile)
        updatePersonalData(profile)
        updateStats(profile)
        updateFollowing(profile)
        profileSwipeRefresh.isRefreshing = false
    }

    /**
     * Updates the state of the "Follow" control, depending on whether or not this profile is followed by the app user
     */
    private fun updateFollowing(profile: UserMetadataModel) {
        follow.isActivated = !profile.isSubscribed
        follow.setText(if (profile.isSubscribed) R.string.unfollow else R.string.follow)
        follow.setCompoundDrawablesWithIntrinsicBounds(
            null,
            AppCompatDrawableManager.get().getDrawable(
                requireContext(),
                if (!profile.isSubscribed) R.drawable.ic_following_active else R.drawable.ic_following_inactive
            ),
            null, null
        )

    }

    /**
     * Updates profile stats like count of the followers and following (including both users and followers)
     */
    private fun updateStats(profile: UserMetadataModel) {
        followersCount.text = (profile.subscribers.usersCount + profile.subscribers.communitiesCount).toString()
        followingsCount.text = (profile.subscriptions.usersCount + profile.subscriptions.communitiesCount).toString()
    }

    /**
     * Updates personal data like username, avatar cover etc
     */
    private fun updatePersonalData(profile: UserMetadataModel) {
        if (profile.personal.coverUrl.isNullOrBlank())
            cover.setImageDrawable(null)
        else Glide.with(requireContext()).load(profile.personal.coverUrl)
            .into(cover)

        avatarText.text = profile.username

        if (profile.personal.avatarUrl.isNullOrBlank()) {
            avatar.setImageDrawable(null)
            avatarText.visibility = View.VISIBLE
        } else {
            Glide.with(requireContext())
                .load(profile.personal.avatarUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(avatar)
            avatarText.visibility = View.GONE
        }

        username.text = profile.username

        joined.text = String.format(
            getString(R.string.profile_creation_date_caption_format),
            SimpleDateFormat(
                getString(R.string.profile_creation_date_format),
                Locale.US
            ).format(profile.createdAt)
        )

        bio.text = profile.personal.biography
    }

    /**
     * Changes some controls visibility depending on whether or not this profile is the actual app user profile
     */
    private fun updateControlsAccessibility(isActiveUserProfile: Boolean) {
        if (isActiveUserProfile) {
            updatePhoto.visibility = View.VISIBLE
            updateCover.visibility = View.VISIBLE
            settings.visibility = View.VISIBLE
            follow.visibility = View.GONE
            sentPoints.visibility = View.GONE
        } else {
            updatePhoto.visibility = View.GONE
            updateCover.visibility = View.GONE
            settings.visibility = View.GONE
            follow.visibility = View.VISIBLE
            sentPoints.visibility = View.VISIBLE
            editBio.visibility = View.GONE
            ivBack.visibility = View.VISIBLE
            bio.setOnClickListener { }
            editBio.setOnClickListener { }
        }
    }

    /**
     * Sets click listeners on Edit Bio button
     */
    private fun updateEditBioControls(profile: UserMetadataModel) {
        listOf(bio, editBio).forEach {
            it.setOnClickListener {
                startActivity(
                    EditProfileBioActivity.getIntent(
                        requireContext(),
                        EditProfileBioFragment.Args(
                            profile.userId.name,
                            profile.personal.biography ?: ""
                        )
                    )
                )
            }
        }
    }

    private fun setupTabLayout() {
        TabLayoutMediator(tabLayout, profilePager) { tab, position ->
            tab.setText(Tab.values()[position].title)
        }.attach()
    }

    private fun setupViewPager() {
        profilePager.adapter = object : FragmentStateAdapter(requireFragmentManager(), this.lifecycle) {
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    Tab.POSTS.index -> UserPostsFeedFragment.newInstance(getUserName()).apply {
                        setTargetFragment(this@OldProfileFragment,
                            REQUEST_FEED
                        )
                    }
                    Tab.COMMENTS.index -> UserPostsFeedFragment.newInstance(getUserName()).apply {
                        setTargetFragment(this@OldProfileFragment,
                            REQUEST_FEED
                        )
                    }
                    else -> throw RuntimeException("Unsupported tab")
                }
            }

            override fun getItemCount() = Tab.values().size

        }
    }

    override fun provideEventsLiveData(): LiveData<FeedViewModel.Event> = viewModel.getEventsLiveData

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_UPDATE_COVER_DIALOG) {
            val target = when (resultCode) {
                ImagePickerDialog.RESULT_GALLERY ->
                    ImagePickerFragmentBase.ImageSource.GALLERY
                ImagePickerDialog.RESULT_CAMERA ->
                    ImagePickerFragmentBase.ImageSource.CAMERA
                ImagePickerDialog.RESULT_DELETE -> {
                    viewModel.clearProfileCover()
                    null
                }
                else -> null
            }
            if (target != null) startActivityForResult(
                EditProfileCoverActivity
                    .getIntent(
                        requireContext(),
                        EditProfileCoverFragment.Args(viewModel.forUser.name, target)
                    ), REQUEST_UPDATE_COVER
            )
        }

        if (requestCode == REQUEST_UPDATE_PHOTO_DIALOG) {
            val target = when (resultCode) {
                ImagePickerDialog.RESULT_GALLERY ->
                    ImagePickerFragmentBase.ImageSource.GALLERY
                ImagePickerDialog.RESULT_CAMERA ->
                    ImagePickerFragmentBase.ImageSource.CAMERA
                ImagePickerDialog.RESULT_DELETE -> {
                    viewModel.clearProfileAvatar()
                    null
                }
                else -> null
            }
            if (target != null) startActivityForResult(
                EditProfileAvatarActivity
                    .getIntent(
                        requireContext(),
                        EditProfileAvatarFragment.Args(viewModel.forUser.name, target)
                    ), REQUEST_UPDATE_PHOTO
            )
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(OldProfileViewModel::class.java)
    }

    private fun getUserName() = (arguments?.getString(Tags.USER_ID) ?: "")

    companion object {
        fun newInstance(userId: String) = OldProfileFragment().apply {
            arguments = Bundle().apply {
                putString(Tags.USER_ID, userId)
            }
        }
    }
}
