package io.golos.domain.interactors.user

import javax.inject.Inject

class UnsubscribeToFollowerUseCaseImpl @Inject constructor(private val userRepository: UserRepository): UnsubscribeToFollowerUseCase {

    override suspend fun unsubscribeToFollower(userId: String) {
        userRepository.unsubscribeToFollower(userId)
    }
}