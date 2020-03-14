package io.golos.cyber_android.ui.screens.communities_list.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.golos.cyber_android.ui.shared.mvvm.model.ModelBaseImpl
import io.golos.cyber_android.ui.shared.recycler_view.versioned.VersionedListItem
import io.golos.cyber_android.ui.shared.recycler_view.versioned.CommunityListItem
import io.golos.cyber_android.ui.shared.recycler_view.versioned.LoadingListItem
import io.golos.cyber_android.ui.shared.recycler_view.versioned.RetryListItem
import io.golos.domain.DispatchersProvider
import io.golos.domain.GlobalConstants
import io.golos.domain.dependency_injection.Clarification
import io.golos.domain.dto.CommunityDomain
import io.golos.domain.dto.CommunityIdDomain
import io.golos.domain.dto.UserIdDomain
import io.golos.domain.use_cases.community.CommunitiesRepository
import io.golos.utils.id.IdUtil
import io.golos.utils.id.MurmurHash
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

open class CommunitiesListModelImpl
@Inject
constructor(
    private val userId: UserIdDomain,
    @Named(Clarification.SHOW_ALL)
    private val showAll: Boolean,
    private val communitiesRepository: CommunitiesRepository,
    private val dispatchersProvider: DispatchersProvider
) : ModelBaseImpl(), CommunitiesListModel {

    private enum class LoadingState {
        READY_TO_LOAD,
        LOADING,
        ALL_DATA_LOADED,
        IN_ERROR
    }

    private var currentLoadingState = LoadingState.READY_TO_LOAD

    private var subscribingInProgress = false

    private var loadedItems: MutableList<VersionedListItem> = mutableListOf()

    private val _items = MutableLiveData<List<VersionedListItem>>(listOf())
    override val items: LiveData<List<VersionedListItem>> get() = _items

    override val pageSize = GlobalConstants.PAGE_SIZE

    override suspend fun loadPage() {
        when(currentLoadingState) {
            LoadingState.READY_TO_LOAD -> loadData(false)

            LoadingState.LOADING,
            LoadingState.IN_ERROR,
            LoadingState.ALL_DATA_LOADED -> { /* do nothing */ }
        }
    }

    override fun clear(): Boolean {
        if(currentLoadingState == LoadingState.LOADING || currentLoadingState == LoadingState.IN_ERROR) {
            return false
        }

        loadedItems.clear()

        return true
    }

    override suspend fun retry() {
        when(currentLoadingState) {
            LoadingState.IN_ERROR -> loadData(true)

            LoadingState.LOADING,
            LoadingState.READY_TO_LOAD,
            LoadingState.ALL_DATA_LOADED -> { /* do nothing */ }
        }
    }

    override suspend fun subscribeUnsubscribe(communityCode: String): Boolean {
        if(subscribingInProgress) {
            return true
        }

        subscribingInProgress = true

        val community = loadedItems[getCommunityIndex(communityCode)] as CommunityListItem

        setCommunityInProgress(communityCode)

        val isSuccess = withContext(dispatchersProvider.ioDispatcher) {
            try {
                if(community.isInPositiveState) {
                    communitiesRepository.unsubscribeToCommunity(CommunityIdDomain(communityCode, null))
                } else {
                    communitiesRepository.subscribeToCommunity(CommunityIdDomain(communityCode, null))
                }
                true
            } catch (ex: Exception) {
                Timber.e(ex)
                false
            }
        }

        completeCommunityInProgress(communityCode, isSuccess)

        subscribingInProgress = false

        return isSuccess
    }

    protected fun CommunityDomain.map() =
        CommunityListItem(
            MurmurHash.hash64(this.communityId),
            0,
            isFirstItem = false,
            isLastItem = false,
            community = this,
            isInPositiveState = this.isSubscribed,
            isProgress = false
        )

    private suspend fun loadData(isRetry: Boolean) {
        currentLoadingState = LoadingState.LOADING

        if(isRetry) {
            replaceRetryByLoading()
        } else {
            addLoading()
        }

        val data = getData(loadedItems.size-1)
        data?.forEach {
            Log.d("COMMUNITY_TEST", it.community.toString())
        }

        currentLoadingState = if(data != null) {
            addLoadedData(data)

            if(data.size < pageSize) LoadingState.ALL_DATA_LOADED else LoadingState.READY_TO_LOAD
        } else {
            replaceLoadingByRetry()
            LoadingState.IN_ERROR
        }
    }

    private suspend fun getData(offset: Int): List<CommunityListItem>? =
        try {
            communitiesRepository
                .getCommunitiesList(userId, offset, pageSize, showAll)
                .map { rawItem -> rawItem.map() }
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }

    private fun updateData(updateAction: (MutableList<VersionedListItem>) -> Unit) {
        updateAction(loadedItems)
        _items.value = loadedItems
    }

    private fun addLoading() = updateData { loadedItems.add(
        LoadingListItem(
            IdUtil.generateLongId(),
            0
        )
    ) }

    private fun replaceRetryByLoading() = updateData {
        loadedItems[loadedItems.lastIndex] =
            LoadingListItem(IdUtil.generateLongId(), 0)
    }

    private fun replaceLoadingByRetry() = updateData {
        loadedItems[loadedItems.lastIndex] =
            RetryListItem(IdUtil.generateLongId(), 0)
    }

    private fun addLoadedData(data: List<CommunityListItem>) = updateData {
        loadedItems.removeAt(loadedItems.lastIndex)

        if(loadedItems.isNotEmpty()) {
            val lastItem = loadedItems.last()
            if(lastItem is CommunityListItem) {
                loadedItems[loadedItems.lastIndex] = lastItem.copy(isLastItem = false)
            }
        }

        if(data.isNotEmpty()) {
            val updatableData = data.toMutableList()
            val lastItem = updatableData.last()
            updatableData[updatableData.lastIndex] = lastItem.copy(isLastItem = true)
            loadedItems.addAll(updatableData)
        }
    }

    private fun setCommunityInProgress(communityId: String) =
        updateCommunity(communityId) { oldCommunity ->
            oldCommunity.copy(
                version = oldCommunity.version + 1,
                isProgress = true,
                isInPositiveState = !oldCommunity.isInPositiveState)
        }

    private fun completeCommunityInProgress(communityId: String, isSuccess: Boolean) =
        updateCommunity(communityId) { oldCommunity ->
            oldCommunity.copy(
                version = oldCommunity.version + 1,
                isProgress = false,
                isInPositiveState = if(isSuccess) oldCommunity.isInPositiveState else !oldCommunity.isInPositiveState
            )
        }

    private fun updateCommunity(communityId: String, updateAction: (CommunityListItem) -> CommunityListItem) = updateData {
        val itemIndex = getCommunityIndex(communityId)
        loadedItems[itemIndex] = updateAction(loadedItems[itemIndex] as CommunityListItem)
    }

    private fun getCommunityIndex(communityId: String) =
        loadedItems.indexOfFirst { it is CommunityListItem && it.community.communityId == communityId }
}