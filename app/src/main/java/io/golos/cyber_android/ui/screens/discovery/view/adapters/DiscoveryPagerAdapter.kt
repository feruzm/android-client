package io.golos.cyber_android.ui.screens.discovery.view.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.golos.cyber_android.ui.shared.base.FragmentBase
import io.golos.cyber_android.ui.shared.mvvm.FragmentBaseMVVM

class DiscoveryPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val fragments:List<FragmentPagerModel>
) :
    FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position].fragment
    }

    data class FragmentPagerModel(val fragment:Fragment,val title:String)

}