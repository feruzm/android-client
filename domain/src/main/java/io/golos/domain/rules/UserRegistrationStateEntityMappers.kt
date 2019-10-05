package io.golos.domain.rules

import io.golos.commun4j.services.model.FirstRegistrationStepResult
import io.golos.commun4j.services.model.UserRegistrationState
import io.golos.commun4j.services.model.UserRegistrationStateResult
import io.golos.domain.entities.*
import io.golos.domain.requestmodel.RegistrationStepRequest
import io.golos.domain.requestmodel.SetUserKeysRequest
import java.util.*
import javax.inject.Inject

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-04-11.
 */
class UserRegistrationStateRelatedData(
    val requestResult: Any?,
    val request: RegistrationStepRequest,
    val stateRequestResult: UserRegistrationStateResult
)

class UserRegistrationStateEntityMapper
@Inject
constructor() : CyberToEntityMapper<UserRegistrationStateRelatedData, UserRegistrationStateEntity> {
    override suspend fun invoke(cyberObject: UserRegistrationStateRelatedData): UserRegistrationStateEntity {
        val stateRequestResult = cyberObject.stateRequestResult
        val requestResult = cyberObject.requestResult
        val stateRequest = cyberObject.request

        return when (cyberObject.stateRequestResult.state
            ?: throw IllegalArgumentException("server didn't returned reg state of user")) {

            UserRegistrationState.REGISTERED -> RegisteredUser(
                stateRequestResult.user ?: throw IllegalStateException(
                    "server" +
                            "didn't returned user name for some reason"
                ),
                (stateRequest as? SetUserKeysRequest)?.masterKey
            )
            UserRegistrationState.TO_BLOCK_CHAIN -> UnWrittenToBlockChainUser(
                stateRequestResult.user ?: throw IllegalStateException(
                    "server" +
                            "didn't returned user name for some reason"
                )
            )
            UserRegistrationState.VERIFY -> {
                val firstStepResult = requestResult as? FirstRegistrationStepResult
                return if (firstStepResult == null) UnverifiedUser(Date(Long.MIN_VALUE))
                else {
                    UnverifiedUser(requestResult.nextSmsRetry, requestResult.code)
                }
            }
            UserRegistrationState.SET_USER_NAME -> VerifiedUserWithoutUserName()
            UserRegistrationState.FIRST_STEP -> UnregisteredUser()
        }
    }
}