package io.golos.cyber_android.ui.screens.ftue.model

import io.golos.cyber_android.ui.shared.mvvm.model.ModelBaseImpl
import io.golos.domain.dto.FtueBoardStageDomain
import io.golos.domain.repositories.UsersRepository
import javax.inject.Inject

class FtueModelImpl @Inject constructor(private val usersRepository: UsersRepository): FtueModel, ModelBaseImpl() {

    override suspend fun getFtueBoardStage(): FtueBoardStageDomain = usersRepository.getFtueBoardStage()

    override suspend fun setFtueBoardStage(stage: FtueBoardStageDomain) {
        usersRepository.setFtueBoardStage(stage)
    }
}