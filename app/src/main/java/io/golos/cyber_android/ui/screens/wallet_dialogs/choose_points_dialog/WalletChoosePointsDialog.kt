package io.golos.cyber_android.ui.screens.wallet_dialogs.choose_points_dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import io.golos.cyber_android.ui.screens.wallet.dto.MyPointsListItem
import io.golos.cyber_android.ui.screens.wallet_dialogs.WalletListDialogBase
import io.golos.cyber_android.ui.screens.wallet_dialogs.choose_points_dialog.list.WalletChoosePointsDialogAdapter
import io.golos.cyber_android.ui.screens.wallet_dialogs.choose_points_dialog.list.WalletChoosePointsDialogItemEventsProcessor
import io.golos.domain.GlobalConstants
import io.golos.domain.dto.CommunityIdDomain
import io.golos.domain.dto.WalletCommunityBalanceRecordDomain
import io.golos.utils.id.IdUtil

class WalletChoosePointsDialog : WalletListDialogBase<CommunityIdDomain, WalletChoosePointsDialogAdapter>(), WalletChoosePointsDialogItemEventsProcessor {
    companion object {
        private const val BALANCE = "BALANCE"

        fun show(parent: Fragment, balance: List<WalletCommunityBalanceRecordDomain>, closeAction: (CommunityIdDomain?) -> Unit) =
            WalletChoosePointsDialog()
                .apply {
                    closeActionListener = closeAction
                    arguments = Bundle().apply {
                        putParcelableArray(BALANCE, balance.toTypedArray())
                    }
                }
                .show(parent.parentFragmentManager, "CHOOSE_POINTS")
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateList(arguments!!.getParcelableArray(BALANCE)!!.toList() as List<WalletCommunityBalanceRecordDomain>)
    }

    private fun updateList(balance: List<WalletCommunityBalanceRecordDomain>) =
        updateListData(balance.map { MyPointsListItem(IdUtil.generateLongId(), 0, false, false,
            it.communityId.code == GlobalConstants.COMMUN_CODE, it) })

    override fun onItemClick(communityId: CommunityIdDomain) {
        closeActionListener(communityId)
        isItemSelected = true
        dismiss()
    }

    override fun provideAdapter() = WalletChoosePointsDialogAdapter(this)
}