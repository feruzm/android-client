package io.golos.cyber_android.ui.screens.wallet.di

import dagger.Subcomponent
import io.golos.cyber_android.ui.screens.wallet.view.WalletFragment
import io.golos.cyber_android.ui.screens.wallet_choose_friend_dialog.di.WalletChooseFriendDialogComponent
import io.golos.cyber_android.ui.screens.wallet_point.di.WalletPointFragmentComponent
import io.golos.domain.dependency_injection.scopes.FragmentScope

@Subcomponent(modules = [WalletFragmentModuleBinds::class, WalletFragmentModule::class])
@FragmentScope
interface WalletFragmentComponent {
    @Subcomponent.Builder
    interface Builder {
        fun init(module: WalletFragmentModule): Builder
        fun build(): WalletFragmentComponent
    }

    val pointFragment: WalletPointFragmentComponent.Builder
    val chooseFriendDialog: WalletChooseFriendDialogComponent.Builder

    fun inject(fragment: WalletFragment)
}