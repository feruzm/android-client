package io.golos.cyber_android.ui.screens.wallet.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.screens.wallet.dto.MyPointsListItem
import io.golos.cyber_android.ui.screens.wallet.model.WalletModel
import io.golos.cyber_android.ui.screens.wallet.view.history.WalletHistoryListItemEventsProcessor
import io.golos.cyber_android.ui.screens.wallet.view.my_points.WalletMyPointsListItemEventsProcessor
import io.golos.cyber_android.ui.screens.wallet.view.send_points.WalletSendPointsListItemEventsProcessor
import io.golos.cyber_android.ui.shared.mvvm.viewModel.ViewModelBase
import io.golos.cyber_android.ui.shared.mvvm.view_commands.NavigateBackwardCommand
import io.golos.cyber_android.ui.shared.mvvm.view_commands.ShowMessageResCommand
import io.golos.cyber_android.ui.shared.recycler_view.versioned.VersionedListItem
import io.golos.domain.DispatchersProvider
import io.golos.domain.dependency_injection.Clarification
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class WalletViewModel
@Inject
constructor(
    dispatchersProvider: DispatchersProvider,
    model: WalletModel
) : ViewModelBase<WalletModel>(dispatchersProvider, model),
    WalletMyPointsListItemEventsProcessor,
    WalletSendPointsListItemEventsProcessor,
    WalletHistoryListItemEventsProcessor {

    private val _swipeRefreshing = MutableLiveData<Boolean>(false)
    val swipeRefreshing: LiveData<Boolean> get() = _swipeRefreshing

    private val _totalValue = MutableLiveData<Double>(0.0)
    val totalValue: LiveData<Double> = _totalValue

    private val _myPointsItems = MutableLiveData<List<MyPointsListItem>>(listOf())
    val myPointsItems: LiveData<List<MyPointsListItem>> = _myPointsItems

    val sendPointItems: LiveData<List<VersionedListItem>> = model.sendPointItems

    val historyItems: LiveData<List<VersionedListItem>> = model.historyItems

    val pageSize = model.pageSize

    private var loadPageJob: Job? = null

    init {
        loadPage(false)
    }

    fun onSwipeRefresh() = loadPage(true)

    fun onBackClick() {
        _command.value = NavigateBackwardCommand()
    }

    override fun onSendPointsNextPageReached() {
        launch {
            model.loadSendPointsPage()
        }
    }

    override fun onSendPointsRetryClick() {
        launch {
            model.retrySendPointsPage()
        }
    }

    override fun onHistoryNextPageReached() {
        launch {
            model.loadHistoryPage()
        }
    }

    override fun onHistoryRetryClick() {
        launch {
            model.retryHistoryPage()
        }
    }

    private fun loadPage(needReload: Boolean) {
        loadPageJob?.cancel()
        loadPageJob = launch {
            try {
                if(needReload) {
                    model.clearSendPoints()
                    model.clearHistory()
                }

                model.initBalance(needReload)

                _totalValue.value = model.totalBalance
                _myPointsItems.value = model.getMyPointsItems()

                // To load the very first page
                onSendPointsNextPageReached()
                onHistoryNextPageReached()
            } catch (ex: Exception) {
                Timber.e(ex)

                _command.value = ShowMessageResCommand(R.string.common_general_error)
                _command.value = NavigateBackwardCommand()
            } finally {
                if(needReload) {
                    _swipeRefreshing.value = false
                }
            }
        }
    }
}