package io.golos.cyber_android.ui.mappers

import io.golos.cyber_android.ui.screens.post_filters.PostFiltersHolder
import io.golos.domain.dto.PostsConfigurationDomain

fun PostFiltersHolder.UpdateTimeFilter.mapToTypeFeedDomain(): PostsConfigurationDomain.TypeFeedDomain{
    return when (this) {
        PostFiltersHolder.UpdateTimeFilter.HOT ->
            PostsConfigurationDomain.TypeFeedDomain.TOP_REWARDS
        PostFiltersHolder.UpdateTimeFilter.NEW ->
            PostsConfigurationDomain.TypeFeedDomain.NEW
        PostFiltersHolder.UpdateTimeFilter.POPULAR ->
            PostsConfigurationDomain.TypeFeedDomain.TOP_LIKES
    }
}

fun PostsConfigurationDomain.TypeFeedDomain.mapToFilterTypeDomain(): PostFiltersHolder.UpdateTimeFilter {
    return when (this) {
        PostsConfigurationDomain.TypeFeedDomain.NEW -> PostFiltersHolder.UpdateTimeFilter.NEW
        PostsConfigurationDomain.TypeFeedDomain.TOP_LIKES -> PostFiltersHolder.UpdateTimeFilter.POPULAR
        PostsConfigurationDomain.TypeFeedDomain.TOP_REWARDS -> PostFiltersHolder.UpdateTimeFilter.HOT
        else -> PostFiltersHolder.UpdateTimeFilter.HOT
    }
}