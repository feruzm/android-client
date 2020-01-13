package io.golos.domain.dto

data class PostsConfigurationDomain(
    val userId: String,
    val communityId: String?,
    val communityAlias: String?,
    val sortBy: SortByDomain = SortByDomain.TIME_DESC,
    val timeFrame: TimeFrameDomain = TimeFrameDomain.ALL,
    val limit: Int,
    val offset: Int,
    val typeFeed: TypeFeedDomain = TypeFeedDomain.NEW,
    val allowNsfw: Boolean = true
) {

    enum class TypeFeedDomain {
        NEW,
        HOT,
        POPULAR
    }

    enum class SortByDomain {
        TIME,
        TIME_DESC
    }

    enum class TimeFrameDomain {
        DAY,
        WEEK,
        MONTH,
        ALL
    }
}