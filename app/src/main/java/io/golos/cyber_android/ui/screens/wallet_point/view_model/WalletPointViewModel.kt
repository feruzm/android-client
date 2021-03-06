package io.golos.cyber_android.ui.screens.wallet_point.view_model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.golos.cyber_android.ui.screens.wallet.data.enums.Currencies
import io.golos.cyber_android.ui.screens.wallet.dto.NavigateToWalletConvertCommand
import io.golos.cyber_android.ui.screens.wallet.dto.NavigateToWalletSendPoints
import io.golos.cyber_android.ui.screens.wallet.dto.ShowFilterDialog
import io.golos.cyber_android.ui.screens.wallet.dto.ShowSendPointsDialog
import io.golos.cyber_android.ui.screens.wallet.model.CurrencyBalance
import io.golos.cyber_android.ui.screens.wallet_point.dto.CarouselStartData
import io.golos.cyber_android.ui.screens.wallet_point.model.WalletPointModel
import io.golos.cyber_android.ui.screens.wallet_shared.history.view.WalletHistoryListItemEventsProcessor
import io.golos.cyber_android.ui.screens.wallet_shared.send_points.list.view.WalletSendPointsListItemEventsProcessor
import io.golos.cyber_android.ui.shared.extensions.getMessage
import io.golos.cyber_android.ui.shared.mvvm.viewModel.ViewModelBase
import io.golos.cyber_android.ui.shared.mvvm.view_commands.NavigateBackwardCommand
import io.golos.cyber_android.ui.shared.mvvm.view_commands.ShowMessageTextCommand
import io.golos.cyber_android.ui.shared.recycler_view.versioned.VersionedListItem
import io.golos.domain.DispatchersProvider
import io.golos.domain.dto.CommunityIdDomain
import io.golos.domain.dto.HistoryFilterDomain
import io.golos.domain.dto.UserBriefDomain
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class WalletPointViewModel
@Inject
constructor(
    private val appContext: Context,
    dispatchersProvider: DispatchersProvider,
    model: WalletPointModel
) : ViewModelBase<WalletPointModel>(dispatchersProvider, model),
    WalletSendPointsListItemEventsProcessor,
    WalletHistoryListItemEventsProcessor {

    private val _availablePoints = MutableLiveData<Double>(0.0)
    val availablePoints: LiveData<Double> = _availablePoints

    private val _holdPoints = MutableLiveData<Double>(0.0)
    val holdPoints: LiveData<Double> = _holdPoints

    private val _availableHoldFactor = MutableLiveData<Double>(0.0)
    val availableHoldFactor: LiveData<Double> = _availableHoldFactor

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _currencyBalance = MutableLiveData(CurrencyBalance(0.0, Currencies.COMMUN))
    val currencyBalance: LiveData<CurrencyBalance> = _currencyBalance

    private val _balanceInCommuns = MutableLiveData<Double>(0.0)
    val balanceInCommuns: LiveData<Double> = _balanceInCommuns

    private val _carouselStartData = MutableLiveData<CarouselStartData>()
    val carouselStartData: LiveData<CarouselStartData> = _carouselStartData

    private val _swipeRefreshing = MutableLiveData<Boolean>(false)
    val swipeRefreshing: LiveData<Boolean> get() = _swipeRefreshing

    val sendPointItems: LiveData<List<VersionedListItem>> = model.sendPointItems

    val historyItems: LiveData<List<VersionedListItem>> = model.historyItems

    val pageSize = model.pageSize

    private var loadPageJob: Job? = null
    private var loadHistoryJob: Job? = null

    init {
        loadPage(false)
    }

    fun onBackClick() {
        _command.value = NavigateBackwardCommand()
    }

    fun onCommunitySelected(communityId: CommunityIdDomain) {
        if(model.switchBalanceRecord(communityId)) {
            updateHeaders(false)

            loadHistoryJob?.cancel()
            loadHistoryJob = launch {
                delay(1500)
                model.clearHistory()
                onHistoryNextPageReached()
            }
        }
    }

    fun onSwipeRefresh() = loadPage(true)

    override fun onSendPointsItemClick(user: UserBriefDomain?) {
        _command.value = NavigateToWalletSendPoints(model.currentBalanceRecord.communityId, user, model.sourceBalance)
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

    override fun onFilterClick() {
        _command.postValue(ShowFilterDialog())
    }

    fun onSeeAllSendPointsClick() {
        _command.value = ShowSendPointsDialog()
    }

    fun onConvertClick() {
        _command.value = NavigateToWalletConvertCommand(model.currentBalanceRecord.communityId, model.sourceBalance)
    }

    private fun loadPage(needReload: Boolean) {
        loadPageJob?.cancel()
        loadHistoryJob?.cancel()

        loadPageJob = launch {
            try {
                if(needReload) {
                    model.clearSendPoints()
                    model.clearHistory()
                }

                model.initBalance(needReload)
                updateHeaders(!needReload)

                // To load the very first page
                onSendPointsNextPageReached()
                onHistoryNextPageReached()
            } catch (ex: Exception) {
                Timber.e(ex)
                _command.value = ShowMessageTextCommand(ex.getMessage(appContext))

                _command.value = NavigateBackwardCommand()
            } finally {
                if(needReload) {
                    _swipeRefreshing.value = false
                }
            }
        }
    }

    private fun updateHeaders(initCarousel: Boolean) {
        _currencyBalance.value = CurrencyBalance(model.balanceInPoints, model.balanceCurrency)
        _availablePoints.value = model.balanceInPoints - model.holdPoints
        _holdPoints.value = model.holdPoints

        _availableHoldFactor.value = if(_availablePoints.value!! != 0.0) _holdPoints.value!! / _availablePoints.value!! else 0.0

        _title.value = model.title
        _balanceInCommuns.value = model.balanceInCommuns

        if(initCarousel) {
            _carouselStartData.value = model.getCarouselStartData()
        }
    }

    fun applyFilters(historyFilterDomain: HistoryFilterDomain) {
        launch {
            try {
                model.applyFilters(historyFilterDomain)
            }catch (e:Exception){
                Timber.e(e)
                _command.value = ShowMessageTextCommand(e.getMessage(appContext))
                _command.value = NavigateBackwardCommand()
            }
        }
    }

    fun getCurrentFilter(): HistoryFilterDomain = model.getCurrentFilter()
}