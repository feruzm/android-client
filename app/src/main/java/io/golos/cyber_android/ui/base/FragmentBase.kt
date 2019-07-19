package io.golos.cyber_android.ui.base

import androidx.fragment.app.Fragment
import io.golos.cyber_android.serviceLocator
import io.golos.cyber_android.ui.common.helper.UIHelper
import io.golos.cyber_android.ui.dialogs.LoadingDialog

/**
 * [Fragment] that supports showing progress with [LoadingDialog] via
 * [showLoading] and [hideLoading] methods
 */
abstract class FragmentBase: Fragment() {

    private val loadingDialog = LoadingDialog()

    private var wasAdded = false

    protected val uiHelper: UIHelper by lazy { requireContext().serviceLocator.uiHelper }

    protected fun setLoadingVisibility(isVisible: Boolean) =
        if(isVisible) {
            showLoading()
        } else {
            hideLoading()
        }

    protected fun showLoading() {
        if (loadingDialog.dialog?.isShowing != true && !loadingDialog.isAdded && !wasAdded) {
            loadingDialog.show(requireFragmentManager(), "loading")
            wasAdded = true
        }
    }

    protected fun hideLoading() {
        if (loadingDialog.fragmentManager != null && wasAdded) {
            loadingDialog.dismiss()
            wasAdded = false
        }
    }
}