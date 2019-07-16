package io.golos.cyber_android.ui.screens.login.fingerprint

import androidx.lifecycle.ViewModel
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.common.SingleLiveData
import io.golos.cyber_android.ui.common.mvvm.ShowMessageCommand
import io.golos.cyber_android.ui.common.mvvm.ViewCommand
import io.golos.domain.DispatchersProvider
import io.golos.domain.entities.AppUnlockWay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class FingerprintViewModel(
    private val dispatchersProvider: DispatchersProvider,
    private val model: FingerprintModel
) : ViewModel(), CoroutineScope {

    private val scopeJob: Job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = scopeJob + dispatchersProvider.uiDispatcher

    val command: SingleLiveData<ViewCommand> = SingleLiveData()

    fun onUnlockViaPinCodeClick() = saveUnlockWay(AppUnlockWay.PIN_CODE)

    fun onUnlockViaFingerprintClick()  = saveUnlockWay(AppUnlockWay.FINGERPRINT)

    override fun onCleared() {
        scopeJob.takeIf { it.isActive }?.cancel()
    }

    private fun saveUnlockWay(appUnlockWay: AppUnlockWay) {
        launch {
            if(model.saveAppUnlockWay(appUnlockWay)) {
                // navigate()
            } else {
                command.value = ShowMessageCommand(R.string.common_general_error)
            }
        }
    }
}