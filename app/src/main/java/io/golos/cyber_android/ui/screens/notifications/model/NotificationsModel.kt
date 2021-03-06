package io.golos.cyber_android.ui.screens.notifications.model

import io.golos.cyber_android.ui.shared.mvvm.model.ModelBase
import io.golos.domain.dto.UserDomain
import io.golos.domain.dto.WalletCommunityBalanceRecordDomain
import io.golos.domain.dto.notifications.NotificationsPageDomain
import io.golos.domain.dto.notifications.NotificationsStatusDomain
import kotlinx.coroutines.flow.Flow
import java.util.*

interface NotificationsModel : ModelBase {

    suspend fun getNotifications(pageKey: String?, limit: Int): NotificationsPageDomain

    suspend fun getNewNotificationsCounter(): Int

    suspend fun markAllNotificationAsViewed(untilDate: Date)

    suspend fun getCurrentUser(): UserDomain

    suspend fun geNewNotificationsCounterFlow(): Flow<NotificationsStatusDomain>

    suspend fun getBalance(): List<WalletCommunityBalanceRecordDomain>
}