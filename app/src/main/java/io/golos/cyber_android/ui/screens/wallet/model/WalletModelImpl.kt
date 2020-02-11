package io.golos.cyber_android.ui.screens.wallet.model

import androidx.lifecycle.LiveData
import io.golos.cyber_android.ui.screens.wallet.dto.MyPointsListItem
import io.golos.cyber_android.ui.screens.wallet.model.lists_workers.history.ListWorkerHistory
import io.golos.cyber_android.ui.screens.wallet.model.lists_workers.send_points.ListWorkerSendPoints
import io.golos.cyber_android.ui.shared.mvvm.model.ModelBaseImpl
import io.golos.cyber_android.ui.shared.recycler_view.versioned.VersionedListItem
import io.golos.data.repositories.wallet.WalletRepository
import io.golos.domain.DispatchersProvider
import io.golos.domain.GlobalConstants
import io.golos.domain.dependency_injection.Clarification
import io.golos.domain.dto.WalletCommunityBalanceRecordDomain
import io.golos.domain.utils.IdUtil
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class WalletModelImpl
@Inject
constructor(
    @Named(Clarification.PAGE_SIZE)
    override val pageSize: Int,
    private val sourceBalance: List<WalletCommunityBalanceRecordDomain>,
    private val dispatchersProvider: DispatchersProvider,
    private val walletRepository: WalletRepository,
    private val listWorkerSendPoints: ListWorkerSendPoints,
    private val listWorkerHistory: ListWorkerHistory
) : ModelBaseImpl(),
    WalletModel {

    private lateinit var balance: List<WalletCommunityBalanceRecordDomain>

    override val totalBalance: Double
        get() = balance.sumByDouble { it.communs ?: 0.0 }

    override val sendPointItems: LiveData<List<VersionedListItem>>
        get() = listWorkerSendPoints.items

    override val historyItems: LiveData<List<VersionedListItem>>
        get() = listWorkerHistory.items

    override suspend fun initBalance(needReload: Boolean) {
        balance = if(needReload) {
            walletRepository.getBalance()
        } else {
            sourceBalance
        }
    }

    override suspend fun getMyPointsItems(): List<MyPointsListItem> =
        withContext(dispatchersProvider.calculationsDispatcher) {
            val communItem = balance.firstOrNull { it.communityId == GlobalConstants.COMMUN_CODE }
                ?: WalletCommunityBalanceRecordDomain(0.0, null, null, null, GlobalConstants.COMMUN_CODE, null, null)

            val result = mutableListOf<MyPointsListItem>()

            result.add(MyPointsListItem(IdUtil.generateLongId(), 0, false, false, true, communItem))

            result.addAll(
                balance
                    .filter { it.communityId != GlobalConstants.COMMUN_CODE }
                    .map { MyPointsListItem(IdUtil.generateLongId(), 0, false, false, false, it) }
            )

            result
        }

    override suspend fun loadSendPointsPage() = listWorkerSendPoints.loadPage()

    override suspend fun retrySendPointsPage() = listWorkerSendPoints.retry()

    override suspend fun clearSendPoints() = listWorkerSendPoints.clear()

    override suspend fun loadHistoryPage() = listWorkerHistory.loadPage()

    override suspend fun retryHistoryPage() = listWorkerHistory.retry()

    override suspend fun clearHistory() = listWorkerHistory.clear()
}