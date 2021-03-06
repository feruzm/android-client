package io.golos.cyber_android.ui.screens.app_start.sign_in.keys_backup.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.screens.app_start.sign_in.keys_backup.dto.ShowBackupWarningDialogCommand
import io.golos.cyber_android.ui.screens.app_start.sign_in.keys_backup.dto.ShowSaveDialogCommand
import io.golos.cyber_android.ui.screens.app_start.sign_in.keys_backup.dto.StartExportToGoogleDriveCommand
import io.golos.cyber_android.ui.screens.app_start.sign_in.keys_backup.model.SignUpProtectionKeysModel
import io.golos.cyber_android.ui.shared.clipboard.ClipboardUtils
import io.golos.cyber_android.ui.shared.mvvm.viewModel.ViewModelBase
import io.golos.cyber_android.ui.shared.mvvm.view_commands.NavigateToMainScreenCommand
import io.golos.cyber_android.ui.shared.mvvm.view_commands.SetLoadingVisibilityCommand
import io.golos.cyber_android.ui.shared.mvvm.view_commands.ShowMessageResCommand
import io.golos.domain.DispatchersProvider
import io.golos.domain.analytics.AnalyticsFacade
import io.golos.domain.analytics.PasswordBackup
import io.golos.domain.dto.UserKeyType
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SignUpProtectionKeysViewModel
@Inject
constructor(
    dispatchersProvider: DispatchersProvider,
    model: SignUpProtectionKeysModel,
    private val clipboardUtils: ClipboardUtils,
    private val analyticsFacade: AnalyticsFacade
) : ViewModelBase<SignUpProtectionKeysModel>(dispatchersProvider, model) {

    private val _masterKey = MutableLiveData<String>()
    val masterKey get() = _masterKey as LiveData<String>

    init {
        launch {
            analyticsFacade.openScreen115()

            model.loadKeys()
            _masterKey.value = model.allKeys.first { it.keyType == UserKeyType.MASTER }.key
        }
    }

    fun onBackupClick() {
        _command.value = ShowSaveDialogCommand()
    }

    fun onSavedClick() {
        _command.value = ShowBackupWarningDialogCommand()
    }

    fun onWarningContinueClick() {
        launch {
            try {
                analyticsFacade.passwordNotBackuped(true)

                model.saveKeysExported()
                model.auth()
                _command.value = NavigateToMainScreenCommand()
            } catch (ex: Exception) {
                _command.value = ShowMessageResCommand(R.string.common_general_error)
            }
        }
    }

    fun onWarningCancelClick() {
        analyticsFacade.passwordNotBackuped(false)
    }

    fun onExportPathSelected(exportPath: String) {
        launch {
            try {
                _command.value = SetLoadingVisibilityCommand(true)

                model.pageRenderer.render(model.getDataForExporting())
                model.copyExportedDocumentTo(exportPath)

                model.saveKeysExported()
                model.auth()

                analyticsFacade.passwordBackuped(PasswordBackup.PDF)

                _command.value = SetLoadingVisibilityCommand(false)
                _command.value = ShowMessageResCommand(R.string.pdf_save_completed, isError = false)
                _command.value = NavigateToMainScreenCommand()
            } catch (ex: Exception) {
                Timber.e(ex)
                _command.value = ShowMessageResCommand(R.string.common_general_error)
                _command.value = SetLoadingVisibilityCommand(false)
            }
        }
    }

    fun onExportToGoogleDriveSelected() {
        launch {
            try {
                model.pageRenderer.render(model.getDataForExporting())
                _command.value = StartExportToGoogleDriveCommand(model.pageRenderer.document!!)
            } catch (ex: Exception) {
                Timber.e(ex)
                _command.value = ShowMessageResCommand(R.string.common_general_error)
            }
        }
    }

    fun onExportToGoogleDriveStarted() {
        _command.value = SetLoadingVisibilityCommand(true)
    }

    fun onExportToGoogleDriveSuccess() {
        launch {
            model.saveKeysExported()
            model.auth()

            analyticsFacade.passwordBackuped(PasswordBackup.CLOUD)

            _command.value = SetLoadingVisibilityCommand(false)

            _command.value = ShowMessageResCommand(R.string.pdf_save_completed, isError = false)

            _command.value = NavigateToMainScreenCommand()
        }
    }

    fun onExportToGoogleDriveFail() {
        _command.value = SetLoadingVisibilityCommand(false)
        _command.value = ShowMessageResCommand(R.string.common_general_error)
    }

    fun onCopyMasterPasswordClick() {
        masterKey.value?.let {
            clipboardUtils.putPlainText(it)
            _command.value = ShowMessageResCommand(R.string.master_password_copied, isError = false)
        }
    }

    override fun onCleared() {
        model.pageRenderer.remove()
        super.onCleared()
    }
}