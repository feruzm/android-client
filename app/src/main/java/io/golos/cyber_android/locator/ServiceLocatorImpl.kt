package io.golos.cyber_android.locator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.golos.cyber4j.Cyber4J
import io.golos.cyber4j.Cyber4JConfig
import io.golos.cyber4j.model.CyberName
import io.golos.cyber_android.BuildConfig
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.common.helpers.UICalculator
import io.golos.cyber_android.ui.common.helpers.UICalculatorImpl
import io.golos.cyber_android.ui.screens.communities.community.CommunityFeedViewModel
import io.golos.cyber_android.ui.screens.editor.EditorPageViewModel
import io.golos.cyber_android.ui.screens.feed.UserSubscriptionsFeedViewModel
import io.golos.cyber_android.ui.screens.login.AuthViewModel
import io.golos.cyber_android.ui.screens.login.signin.SignInViewModel
import io.golos.cyber_android.ui.screens.login.signup.SignUpViewModel
import io.golos.cyber_android.ui.screens.login.signup.country.SignUpCountryViewModel
import io.golos.cyber_android.ui.screens.main.MainViewModel
import io.golos.cyber_android.ui.screens.notifications.NotificationsViewModel
import io.golos.cyber_android.ui.screens.post.PostPageViewModel
import io.golos.cyber_android.ui.screens.profile.ProfileViewModel
import io.golos.cyber_android.ui.screens.profile.edit.avatar.EditProfileAvatarViewModel
import io.golos.cyber_android.ui.screens.profile.edit.bio.EditProfileBioViewModel
import io.golos.cyber_android.ui.screens.profile.edit.cover.EditProfileCoverViewModel
import io.golos.cyber_android.ui.screens.profile.edit.settings.ProfileSettingsViewModel
import io.golos.cyber_android.ui.screens.profile.posts.UserPostsFeedViewModel
import io.golos.cyber_android.utils.FromSpannedToHtmlTransformerImpl
import io.golos.cyber_android.utils.HtmlToSpannableTransformerImpl
import io.golos.cyber_android.utils.ImageCompressorImpl
import io.golos.cyber_android.utils.OnDevicePersister
import io.golos.data.api.Cyber4jApiService
import io.golos.data.errors.CyberToAppErrorMapperImpl
import io.golos.data.repositories.*
import io.golos.domain.DiscussionsFeedRepository
import io.golos.domain.DispatchersProvider
import io.golos.domain.Logger
import io.golos.domain.Repository
import io.golos.domain.entities.*
import io.golos.domain.interactors.action.VoteUseCase
import io.golos.domain.interactors.feed.*
import io.golos.domain.interactors.images.ImageUploadUseCase
import io.golos.domain.interactors.model.*
import io.golos.domain.interactors.notifs.events.EventsUseCase
import io.golos.domain.interactors.publish.DiscussionPosterUseCase
import io.golos.domain.interactors.publish.EmbedsUseCase
import io.golos.domain.interactors.reg.CountriesChooserUseCase
import io.golos.domain.interactors.reg.SignUpUseCase
import io.golos.domain.interactors.settings.SettingsUseCase
import io.golos.domain.interactors.sign.SignInUseCase
import io.golos.domain.interactors.user.UserMetadataUseCase
import io.golos.domain.requestmodel.*
import io.golos.domain.rules.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-03-18.
 */
class ServiceLocatorImpl(private val appContext: Context) : ServiceLocator, RepositoriesHolder {

    private val cyber4jConfigs = mapOf(
        "stable" to Cyber4JConfig(
            blockChainHttpApiUrl = "http://116.202.4.39:8888/",
            servicesUrl = "wss://116.203.98.241:8080"
        ),
        "dev" to Cyber4JConfig(
            blockChainHttpApiUrl = "http://46.4.96.246:8888/",
            servicesUrl = "ws://159.69.33.136:8080"
        ),
        "unstable" to Cyber4JConfig(
            blockChainHttpApiUrl = "http://116.202.4.46:8888/",
            servicesUrl = "wss://159.69.33.136:8080"
        )
    )

    private val cyber4j by lazy { Cyber4J(cyber4jConfigs[BuildConfig.FLAVOR] ?: Cyber4JConfig()) }

    private val apiService: Cyber4jApiService by lazy { Cyber4jApiService(cyber4j) }

    private val cyberPostToEntityMapper = CyberPostToEntityMapper()
    private val voteToEntityMapper = VoteRequestModelToEntityMapper()
    private val cyberFeedToEntityMapper = CyberFeedToEntityMapper(cyberPostToEntityMapper)

    private val fromHtmlTransformet = HtmlToSpannableTransformerImpl()
    private val fromSpannableToHtml = FromSpannedToHtmlTransformerImpl()

    private val deviceIdProvider = MyDeviceIdProvider(appContext)

    private val postEntityToModelMapper = PostEntityEntitiesToModelMapper(fromHtmlTransformet)
    private val feedEntityToModelMapper = PostFeedEntityToModelMapper(postEntityToModelMapper)
    private val voteEntityToPostMapper = VoteRequestEntityToModelMapper()

    private val commentEntityToModelMapper = CommentEntityToModelMapper(fromHtmlTransformet)
    private val commentFeeEntityToModelMapper = CommentsFeedEntityToModelMapper(commentEntityToModelMapper)
    private val toCountriesModelMapper = CountryEntityToModelMapper()

    private val toRegistrationMapper = UserRegistrationStateEntityMapper()

    private val approver = FeedUpdateApprover()

    private val postMerger = PostMerger()
    private val feedMerger = PostFeedMerger()

    private val emptyPostFeedProducer = EmptyPostFeedProducer()

    private val cyberCommentToEntityMapper = CyberCommentToEntityMapper()
    private val cyberCommentFeedToEntityMapper = CyberCommentsToEntityMapper(cyberCommentToEntityMapper)

    private val commentApprover = CommentUpdateApprover()

    private val commentMerger = CommentMerger()
    private val commentFeedMerger = CommentFeedMerger()

    private val emptyCommentFeedProducer = EmptyCommentFeedProducer()

    private val fromIframelyMapper = IfremlyEmbedMapper()
    private val fromOEmbedMapper = OembedMapper()

    private val discussionCreationToEntityMapper = DiscussionCreateResultToEntityMapper()
    private val discussionUpdateToEntityMapper = DiscussionUpdateResultToEntityMapper()
    private val discussionDeleteToEntityMapper = DiscussionDeleteResultToEntityMapper()
    private val discussionEntityRequestToApiRequestMapper = RequestEntityToArgumentsMapper()

    private val toAppErrorMapper = CyberToAppErrorMapperImpl()

    private val logger = object : Logger {
        override fun invoke(e: Exception) {
            e.printStackTrace()
        }
    }
    private val persister = OnDevicePersister(appContext, logger)

    override val dispatchersProvider = object : DispatchersProvider {
        override val uiDispatcher: CoroutineDispatcher
            get() = Dispatchers.Main
        override val workDispatcher: CoroutineDispatcher
            get() = Dispatchers.Default
        override val networkDispatcher: CoroutineDispatcher
            get() = Dispatchers.Default
    }

    override val moshi: Moshi by lazy { Moshi.Builder().build() }

    override val postFeedRepository: AbstractDiscussionsRepository<PostEntity, PostFeedUpdateRequest>by lazy {
        PostsFeedRepository(
            apiService,
            cyberFeedToEntityMapper,
            cyberPostToEntityMapper,
            postMerger,
            feedMerger,
            approver,
            emptyPostFeedProducer,
            dispatchersProvider,
            logger
        )
    }
    override val commentsRepository: DiscussionsFeedRepository<CommentEntity, CommentFeedUpdateRequest> by lazy {
        CommentsFeedRepository(
            apiService,
            cyberCommentFeedToEntityMapper,
            cyberCommentToEntityMapper,
            commentMerger,
            commentFeedMerger,
            commentApprover,
            emptyCommentFeedProducer,
            dispatchersProvider,
            logger
        )
    }

    override val discussionCreationRepository: Repository<DiscussionCreationResultEntity, DiscussionCreationRequestEntity>
            by lazy {
                DiscussionCreationRepository(
                    apiService,
                    apiService,
                    dispatchersProvider,
                    logger,
                    discussionEntityRequestToApiRequestMapper,
                    discussionCreationToEntityMapper,
                    discussionUpdateToEntityMapper,
                    discussionDeleteToEntityMapper,
                    toAppErrorMapper
                )
            }

    override val embedsRepository: Repository<ProcessedLinksEntity, EmbedRequest>
            by lazy {
                EmbedsRepository(apiService, dispatchersProvider, logger, fromIframelyMapper, fromOEmbedMapper)
            }

    override val authRepository: Repository<AuthState, AuthRequest>
            by lazy { AuthStateRepository(apiService, dispatchersProvider, logger, persister) }

    override val voteRepository: Repository<VoteRequestEntity, VoteRequestEntity>
            by lazy {
                VoteRepository(apiService, apiService, dispatchersProvider, logger)
            }
    override val registrationRepository: Repository<UserRegistrationStateEntity, RegistrationStepRequest>
            by lazy {
                RegistrationRepository(
                    apiService, dispatchersProvider, logger, toRegistrationMapper
                )
            }

    override val countriesRepository: Repository<CountriesList, CountriesRequest>
            by lazy {
                CountriesRepository(
                    dispatchersProvider,
                    object : CountriesProvider {
                        override suspend fun getAllCountries(): List<CountryEntity> {
                            val contriesList =
                                appContext.resources.openRawResource(R.raw.countries).readBytes().let { String(it) }
                            return moshi.adapter<List<CountryEntity>>(
                                Types.newParameterizedType(
                                    List::class.java,
                                    CountryEntity::class.java
                                )
                            ).fromJson(contriesList)!!
                        }
                    },
                    logger
                )
            }

    override val settingsRepository: Repository<UserSettingEntity, SettingChangeRequest>
            by lazy {
                SettingsRepository(
                    apiService,
                    SettingsToEntityMapper(moshi),
                    SettingToCyberMapper(),
                    dispatchersProvider,
                    deviceIdProvider,
                    MyDefaultSettingProvider(),
                    logger
                )
            }
    override val imageUploadRepository: Repository<UploadedImagesEntity, ImageUploadRequest>
            by lazy { ImageUploadRepository(apiService, dispatchersProvider, ImageCompressorImpl, logger) }
    override val eventsRepository: Repository<EventsListEntity, EventsFeedUpdateRequest>
            by lazy {
                EventsRepository(
                    apiService, EventsToEntityMapper(), EventsEntityMerger(), EventsApprover(),
                    dispatchersProvider, logger
                )
            }
    override val userMetadataRepository: Repository<UserMetadataCollectionEntity, UserMetadataRequest>
            by lazy {
                UserMetadataRepository(
                    apiService,
                    apiService,
                    dispatchersProvider,
                    logger,
                    UserMetadataToEntityMapper(),
                    toAppErrorMapper
                )
            }

    override fun getCommunityFeedViewModelFactory(
        communityId: CommunityId,
        forUser: CyberName
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return when (modelClass) {
                    CommunityFeedViewModel::class.java -> CommunityFeedViewModel(
                        getCommunityFeedUseCase(communityId),
                        getVoteUseCase(),
                        getDiscussionPosterUseCase(),
                        getUserMetadataUseCase(forUser),
                        getSignInUseCase()
                    ) as T
                    else -> throw IllegalStateException("$modelClass is unsupported")
                }
            }
        }
    }

    override fun getUserPostsFeedViewModelFactory(forUser: CyberUser): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return when (modelClass) {
                    UserPostsFeedViewModel::class.java -> UserPostsFeedViewModel(
                        getUserPostFeedUseCase(forUser),
                        getVoteUseCase(),
                        getDiscussionPosterUseCase(),
                        getSignInUseCase()
                    ) as T


                    else -> throw IllegalStateException("$modelClass is unsupported")
                }
            }
        }
    }

    override fun getUserSubscriptionsFeedViewModelFactory(
        forUser: CyberUser,
        appUser: CyberName
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return when (modelClass) {
                    UserSubscriptionsFeedViewModel::class.java -> UserSubscriptionsFeedViewModel(
                        getUserSubscriptionsFeedUseCase(forUser),
                        getVoteUseCase(),
                        getDiscussionPosterUseCase(),
                        getUserMetadataUseCase(appUser),
                        getSignInUseCase()
                    ) as T
                    else -> throw IllegalStateException("$modelClass is unsupported")
                }
            }
        }
    }

    override fun getPostWithCommentsViewModelFactory(postId: DiscussionIdModel): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return when (modelClass) {
                    PostPageViewModel::class.java -> PostPageViewModel(
                        getPostWithCommentsUseCase(postId),
                        getVoteUseCase(),
                        getDiscussionPosterUseCase(),
                        getSignInUseCase()
                    ) as T
                    else -> throw IllegalStateException("$modelClass is unsupported")
                }
            }
        }
    }

    override fun getDefaultViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return when (modelClass) {
                    SignInViewModel::class.java -> SignInViewModel(
                        getSignInUseCase()
                    ) as T

                    AuthViewModel::class.java -> AuthViewModel(
                        getSignInUseCase()
                    ) as T

                    SignUpCountryViewModel::class.java -> SignUpCountryViewModel(
                        dispatchersProvider,
                        getCountriesChooserUseCase()
                    ) as T

                    SignUpViewModel::class.java -> SignUpViewModel(
                        getSignOnUseCase(true, object : TestPassProvider {
                            override fun provide() = BuildConfig.AUTH_TEST_PASS
                        })
                    ) as T

                    NotificationsViewModel::class.java -> NotificationsViewModel(
                        getEventsUseCase(EventTypeEntity.values().toSet())
                    ) as T

                    ProfileSettingsViewModel::class.java -> ProfileSettingsViewModel(
                        getSettingUserCase(),
                        getSignInUseCase()
                    ) as T

                    MainViewModel::class.java -> MainViewModel(
                        getSignInUseCase(),
                        getEventsUseCase(EventTypeEntity.values().toSet())
                    ) as T
                    else -> throw IllegalStateException("$modelClass is unsupported")
                }
            }
        }
    }

    override fun getEditorPageViewModelFactory(
        community: CommunityModel?,
        postToEdit: DiscussionIdModel?
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return when (modelClass) {
                    EditorPageViewModel::class.java -> EditorPageViewModel(
                        getEmbedsUseCase(),
                        getDiscussionPosterUseCase(),
                        getImageUploadUseCase(),
                        community,
                        postToEdit,
                        if (postToEdit != null) getPostWithCommentsUseCase(postToEdit) else null
                    ) as T
                    else -> throw IllegalStateException("$modelClass is unsupported")
                }
            }
        }
    }

    override fun getViewModelFactoryByCyberName(forUser: CyberName): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return when (modelClass) {
                    EditProfileCoverViewModel::class.java -> EditProfileCoverViewModel(
                        getUserMetadataUseCase(forUser),
                        getImageUploadUseCase(),
                        dispatchersProvider
                    ) as T

                    EditProfileAvatarViewModel::class.java -> EditProfileAvatarViewModel(
                        getUserMetadataUseCase(forUser),
                        getImageUploadUseCase(),
                        dispatchersProvider
                    ) as T

                    EditProfileBioViewModel::class.java -> EditProfileBioViewModel(
                        getUserMetadataUseCase(forUser)
                    ) as T

                    ProfileViewModel::class.java -> ProfileViewModel(
                        getUserMetadataUseCase(forUser),
                        getSignInUseCase(),
                        forUser
                    ) as T
                    else -> throw IllegalStateException("$modelClass is unsupported")
                }
            }
        }
    }

    override fun getCommunityFeedUseCase(communityId: CommunityId): CommunityFeedUseCase {
        return CommunityFeedUseCase(
            communityId,
            postFeedRepository,
            voteRepository,
            feedEntityToModelMapper,
            dispatchersProvider
        )
    }

    override fun getUserSubscriptionsFeedUseCase(user: CyberUser): UserSubscriptionsFeedUseCase {
        return UserSubscriptionsFeedUseCase(
            user,
            postFeedRepository,
            voteRepository,
            feedEntityToModelMapper,
            dispatchersProvider
        )
    }

    override fun getUserPostFeedUseCase(user: CyberUser): UserPostFeedUseCase {
        return UserPostFeedUseCase(
            user,
            postFeedRepository,
            voteRepository,
            feedEntityToModelMapper,
            dispatchersProvider
        )
    }

    override fun getVoteUseCase() =
        VoteUseCase(
            authRepository, voteRepository,
            dispatchersProvider, voteEntityToPostMapper,
            voteToEntityMapper
        )

    override fun getCommentsForAPostUseCase(postId: DiscussionIdModel): PostCommentsFeedUseCase {
        return PostCommentsFeedUseCase(
            postId, commentsRepository, voteRepository, commentFeeEntityToModelMapper,
            dispatchersProvider
        )
    }

    override fun getPostWithCommentsUseCase(postId: DiscussionIdModel): PostWithCommentUseCase {
        return PostWithCommentUseCase(
            postId,
            postFeedRepository,
            postEntityToModelMapper,
            commentsRepository,
            voteRepository,
            commentFeeEntityToModelMapper,
            dispatchersProvider
        )
    }

    override fun getDiscussionPosterUseCase(): DiscussionPosterUseCase {
        return DiscussionPosterUseCase(discussionCreationRepository, dispatchersProvider, fromSpannableToHtml)
    }

    override val getAppContext: Context
        get() = appContext

    override fun getSignInUseCase(): SignInUseCase {
        return SignInUseCase(authRepository, dispatchersProvider)
    }

    override fun getSignOnUseCase(
        isInTestMode: Boolean,
        testPassProvider: TestPassProvider
    ): SignUpUseCase {
        return SignUpUseCase(
            isInTestMode,
            registrationRepository,
            authRepository,
            dispatchersProvider,
            testPassProvider
        )
    }

    override fun getEmbedsUseCase(): EmbedsUseCase {
        return EmbedsUseCase(dispatchersProvider, embedsRepository)
    }

    override fun getCountriesChooserUseCase(): CountriesChooserUseCase {
        return CountriesChooserUseCase(countriesRepository, toCountriesModelMapper, dispatchersProvider)
    }

    override fun getSettingUserCase(): SettingsUseCase {
        return SettingsUseCase(settingsRepository, authRepository)
    }

    override fun getImageUploadUseCase(): ImageUploadUseCase {
        return ImageUploadUseCase(imageUploadRepository)
    }

    override fun getEventsUseCase(eventTypes: Set<EventTypeEntity>): EventsUseCase {
        return EventsUseCase(
            eventTypes,
            eventsRepository,
            authRepository,
            EventEntityToModelMapper(),
            dispatchersProvider
        )
    }

    override fun getUserMetadataUseCase(forUser: CyberName): UserMetadataUseCase {
        return UserMetadataUseCase(forUser, userMetadataRepository)
    }

    override fun getUICalculator(): UICalculator = UICalculatorImpl(appContext)
}