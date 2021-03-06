package io.golos.data.repositories.global_settings_repository

import io.golos.domain.DispatchersProvider
import io.golos.domain.KeyValueStorageFacade
import io.golos.domain.dependency_injection.scopes.ApplicationScope
import io.golos.domain.dto.RewardCurrency
import io.golos.domain.repositories.GlobalSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@ApplicationScope
class GlobalSettingsRepositoryImpl
@Inject constructor(private val dispatchersProvider: DispatchersProvider, private val keyValueStorageFacade: KeyValueStorageFacade) : GlobalSettingsRepository {

    private val rewardCurrencyUpdatesChannel: ConflatedBroadcastChannel<RewardCurrency?> = ConflatedBroadcastChannel(null)
    private val balanceUpdatesChannel: ConflatedBroadcastChannel<Boolean?> = ConflatedBroadcastChannel(null)
    private val currencyUpdatesChannel: ConflatedBroadcastChannel<Boolean?> = ConflatedBroadcastChannel(null)

    override var rewardCurrency: RewardCurrency = RewardCurrency.POINTS
        private set

    override val rewardCurrencyUpdates: Flow<RewardCurrency?>
        get() = rewardCurrencyUpdatesChannel.asFlow()

    override suspend fun loadValues() {
        withContext(dispatchersProvider.ioDispatcher) {
            rewardCurrency = keyValueStorageFacade.getDisplayedRewardCurrency()
        }
    }

    override suspend fun updateRewardCurrency(currency: RewardCurrency) {
        if (currency == rewardCurrency) {
            return
        }

        withContext(dispatchersProvider.ioDispatcher) {
            keyValueStorageFacade.saveDisplayedRewardCurrency(currency)
        }

        rewardCurrency = currency
        rewardCurrencyUpdatesChannel.send(currency)
    }

    override val isBalanceUpdated: Flow<Boolean?>
        get() = balanceUpdatesChannel.asFlow()

    override suspend fun notifyBalanceUpdate(isBalanceUpdated: Boolean?) {
        balanceUpdatesChannel.send(isBalanceUpdated)
    }

    override val isCurrencyUpdated: Flow<Boolean?>
        get() = currencyUpdatesChannel.asFlow()

    override suspend fun notifyCurrencyUpdate(isCurrencyUpdated: Boolean?) {
        currencyUpdatesChannel.send(isCurrencyUpdated)
    }
}