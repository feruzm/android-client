package io.golos.cyber_android.ui.screens.profile.new_profile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.golos.commun4j.utils.toCyberName
import io.golos.cyber_android.R
import io.golos.cyber_android.application.App
import io.golos.cyber_android.application.dependency_injection.graph.app.ui.profile_fragment.ProfileFragmentComponent
import io.golos.cyber_android.databinding.FragmentProfileNewBinding
import io.golos.cyber_android.ui.Tags
import io.golos.cyber_android.ui.common.mvvm.FragmentBaseMVVM
import io.golos.cyber_android.ui.common.mvvm.view_commands.ViewCommand
import io.golos.cyber_android.ui.screens.profile.new_profile.view_model.ProfileViewModel

class ProfileFragment : FragmentBaseMVVM<FragmentProfileNewBinding, ProfileViewModel>() {
    companion object {
        fun newInstance(userId: String) = ProfileFragment().apply {
            arguments = Bundle().apply { putString(Tags.USER_ID, userId) }
        }
    }

    override fun provideViewModelType(): Class<ProfileViewModel> = ProfileViewModel::class.java

    override fun layoutResId(): Int = R.layout.fragment_profile_new

    override fun inject() = App.injections.get<ProfileFragmentComponent>(getUser()).inject(this)

    override fun releaseInjection() {
        App.injections.release<ProfileFragmentComponent>()
    }

    override fun linkViewModel(binding: FragmentProfileNewBinding, viewModel: ProfileViewModel) {
        binding.viewModel = viewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        with(viewModel) {
//            items.observe({viewLifecycleOwner.lifecycle}) { updateList(it) }
//        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel.onViewCreated()
    }

    override fun processViewCommand(command: ViewCommand) {
        super.processViewCommand(command)
//        if(command is NavigateToCommunityPageCommand){
//            val requireActivity = requireActivity()
//            if(requireActivity is MainActivity){
//                requireActivity.showFragment(CommunityPageFragment.newInstance(command.communityId))
//            }
//        }
    }

    private fun getUser() = (arguments?.getString(Tags.USER_ID) ?: "").toCyberName()
}