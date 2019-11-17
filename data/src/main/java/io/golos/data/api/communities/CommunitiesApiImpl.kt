package io.golos.data.api.communities

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.golos.commun4j.Commun4j
import io.golos.data.R
import io.golos.data.api.Commun4jApiBase
import io.golos.domain.repositories.CurrentUserRepositoryRead
import io.golos.domain.AppResourcesProvider
import io.golos.domain.DispatchersProvider
import io.golos.domain.dto.CommunityDomain
import io.golos.domain.dto.CommunityPageDomain
import io.golos.domain.utils.MurmurHash
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

class CommunitiesApiImpl
@Inject
constructor(
    private val context: Context,
    commun4j: Commun4j,
    currentUserRepository: CurrentUserRepositoryRead,
    private val moshi: Moshi,
    private val dispatchersProvider: DispatchersProvider
) : Commun4jApiBase(commun4j, currentUserRepository), CommunitiesApi {

    private val communities: List<CommunityDomain> by lazy { loadCommunities() }

    private data class CommunityRaw (
        val id: String,
        val name: String,
        val followersQuantity: Int,
        val logoUrl: String
    )

    override suspend fun getCommunityPageById(communityId: String): CommunityPageDomain {
        delay(1000)
        //randomException()
        val friends: MutableList<CommunityPageDomain.CommunityFriendDomain> = mutableListOf()
        friends.add(CommunityPageDomain.CommunityFriendDomain(UUID.randomUUID().toString(), "test", "https://images.fastcompany.net/image/upload/w_596,c_limit,q_auto:best,f_auto/fc/3034007-inline-i-applelogo.jpg", true))
        friends.add(CommunityPageDomain.CommunityFriendDomain(UUID.randomUUID().toString(), "test", "https://brandmark.io/logo-rank/random/beats.png", false))

        friends.add(CommunityPageDomain.CommunityFriendDomain(UUID.randomUUID().toString(), "test", "https://brandmark.io/logo-rank/random/pepsi.png", false))

        return CommunityPageDomain(communityId,
            "Binance",
            "https://images.fastcompany.net/image/upload/w_596,c_limit,q_auto:best,f_auto/fc/3034007-inline-i-applelogo.jpg",
            "https://cdn.pixabay.com/photo/2015/12/01/20/28/road-1072823_960_720.jpg",
            "Binance Exchange provides cryptocurrency trading for fintech and blockchain enthusiasts",
            "Binance Exchange provides cryptocurrency trading for fintech and blockchain enthusiasts. Binance Exchange provides cryptocurrency trading for fintech and blockchain enthusiasts",
            false,
            false,
            121,
            friends,
            121000,
            1000,
            CommunityPageDomain.CommunityPageCurrencyDomain("Binance", 1000f),
            Date(System.currentTimeMillis()))
    }

    override fun getCommunitiesList(offset: Int, pageSize: Int, isUser: Boolean): List<CommunityDomain> {
        return try {
            if (Random.nextInt() % 2 == 0) {
                throw java.lang.Exception()
            }

            communities
                .asSequence()
                .filter {
                    if (isUser) {
                        isUserCommunity(it)
                    } else {
                        true
                    }
                }
                .drop(offset)
                .take(pageSize)
                .toList()

        } catch (ex: Exception) {
            Timber.e(ex)
            throw ex
        }
    }

    override fun joinToCommunity(externalId: String) {
        if(Random.nextInt() % 2 == 0) {
            throw java.lang.Exception()
        }
    }

    override suspend fun searchInCommunities(query: String, isUser: Boolean): List<CommunityDomain> =
        withContext(dispatchersProvider.calculationsDispatcher) {
            delay(500)

            val queryLower = query.toLowerCase()

            try {
                communities
                    .asSequence()
                    .filter {
                        if(isUser) {
                            isUserCommunity(it)
                        } else {
                            !isUserCommunity(it)
                        }
                    }
                    .filter { it.name.toLowerCase().contains(queryLower) }
                    .toList()
            } catch(ex: Exception) {
                Timber.e(ex)
                throw ex
            }
        }

    override fun getCommunityById(communityId: String): CommunityDomain? =
            communities.firstOrNull { it.communityId == communityId }

    override suspend fun unsubscribeToCommunity(communityId: String) {
        delay(2000)
        randomException()
    }

    override suspend fun subscribeToCommunity(communityId: String) {
        delay(2000)
        randomException()
    }

    override suspend fun getCommunitiesByQuery(query: String?, offset: Int, pageLimitSize: Int): List<CommunityDomain> {
        delay(2000)
        if(offset == 0){
            val rand = Random
            return if(rand.nextBoolean()){
                getMockCommunitiesList()
            } else{
                emptyList()
            }
        }
        randomException()
        return getMockCommunitiesList()
    }

    private fun loadCommunities(): List<CommunityDomain> {
        val random = Random(Date().time)

        return String(context.resources.openRawResource(R.raw.communities).readBytes())
            .let {
                moshi.adapter<List<CommunityRaw>>(
                    Types.newParameterizedType(
                        List::class.java,
                        CommunityRaw::class.java
                    )
                ).fromJson(it)!!
            }
            .sortedBy {
                it.name
            }
            .map {
                val followersQuantity = when {
                    it.followersQuantity < 10 -> it.followersQuantity * random.nextInt(5)
                    it.followersQuantity < 100 -> it.followersQuantity * random.nextInt(50)
                    else -> it.followersQuantity * random.nextInt(500)
                }
                CommunityDomain(it.id, it.name, it.logoUrl, followersQuantity.toLong(), followersQuantity.toLong(), Random.nextBoolean())
            }
    }

    private fun isUserCommunity(communityExt: CommunityDomain) = MurmurHash.hash64(communityExt.name) % 2 == 0L

    private fun randomException(){
        val rand = Random
        if(rand.nextBoolean()){
            throw RuntimeException()
        }
    }

    override suspend fun getRecommendedCommunities(offset: Int, pageLimitSize: Int): List<CommunityDomain> {
        delay(2000)
        randomException()
        return getMockCommunitiesList()
    }

    private fun getMockCommunitiesList(): List<CommunityDomain> {
        val communityNamesArray = mutableListOf<String>()
        communityNamesArray.add("Overwatch")
        communityNamesArray.add("Commun")
        communityNamesArray.add("ADME")
        communityNamesArray.add("Dribbble")
        communityNamesArray.add("Behance")

        val communityLogoArray = mutableListOf<String>()
        communityLogoArray.add("https://images.fastcompany.net/image/upload/w_596,c_limit,q_auto:best,f_auto/fc/3034007-inline-i-applelogo.jpg")
        communityLogoArray.add("https://brandmark.io/logo-rank/random/beats.png")
        communityLogoArray.add("https://brandmark.io/logo-rank/random/pepsi.png")
        communityLogoArray.add("https://99designs-start-attachments.imgix.net/alchemy-pictures/2019%2F01%2F31%2F23%2F04%2F58%2Ff99d01d7-bf50-4b79-942f-605d6ed1fdce%2Fludibes.png?auto=format&ch=Width%2CDPR&w=250&h=250")

        val communityList =  mutableListOf<CommunityDomain>()
        val rand = Random

        for(i in 0..30){
            val communityName = communityNamesArray[rand.nextInt(communityNamesArray.size - 1)]
            val communityLogo: String = communityLogoArray[rand.nextInt(communityLogoArray.size - 1)]
            val communityDomain = CommunityDomain(
                UUID.randomUUID().toString(),
                communityName,
                communityLogo,
                rand.nextInt(1000000).toLong(),
                rand.nextInt(1000000).toLong(),
                rand.nextBoolean())
            communityList.add(communityDomain)
        }
        return communityList
    }
}