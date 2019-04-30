package io.golos.cyber_android.ui.screens.login.signup.name

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.golos.cyber_android.R
import io.golos.cyber_android.safeNavigate
import io.golos.cyber_android.ui.screens.login.signup.BaseSignUpScreenFragment
import io.golos.cyber_android.utils.asEvent
import io.golos.cyber_android.views.utils.ViewUtils
import io.golos.domain.interactors.model.NextRegistrationStepRequestModel
import io.golos.domain.interactors.model.SetUserNameRequestModel
import io.golos.domain.interactors.model.UnWrittenToBlockChainUserModel
import io.golos.domain.interactors.model.WriteUserToBlockChainRequestModel
import io.golos.domain.requestmodel.QueryResult
import kotlinx.android.synthetic.main.fragment_sign_up_name.*

class SignUpNameFragment : BaseSignUpScreenFragment<SignUpNameViewModel>(SignUpNameViewModel::class.java) {

    override val fieldToValidate: EditText?
        get() = username
    override val continueButton: View
        get() = next

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up_name, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        username.filters = arrayOf(InputFilter.LengthFilter(SignUpNameViewModel.USERNAME_LENGTH))

        back.setOnClickListener { findNavController().navigateUp() }
        next.setOnClickListener {
            viewModel.getFieldIfValid()?.let {
                signUpViewModel.sendName(it)
            }
        }

        username.post {
            ViewUtils.showKeyboard(username)
        }
    }

    override fun observeViewModel() {
        super.observeViewModel()

        signUpViewModel.getUpdatingStateForStep<SetUserNameRequestModel>().observe(this, Observer {
            when (it) {
                is QueryResult.Loading -> showLoading()
                is QueryResult.Error -> onError(it)
            }
        })

        signUpViewModel.stateLiveData.observe(this, Observer { event ->
            event.getIfNotHandled()?.let {
                if (it is UnWrittenToBlockChainUserModel)
                    signUpViewModel.writeToBlockchain()
            }
        })

        signUpViewModel.getUpdatingStateForStep<WriteUserToBlockChainRequestModel>().observe(this, Observer {
            when (it) {
                is QueryResult.Loading -> showLoading()
                is QueryResult.Error -> onError(it)
            }
        })

        signUpViewModel.keysLiveData.asEvent().observe(this, Observer {
            onSuccess()
        })
    }

    private fun onSuccess() {
        hideLoading()
        findNavController().safeNavigate(
            R.id.signUpNameFragment,
            R.id.action_signUpNameFragment_to_signUpKeyFragment
        )
    }

    private fun onError(errorResult: QueryResult.Error<NextRegistrationStepRequestModel>) {
        hideLoading()
        Toast.makeText(requireContext(), errorResult.error.message, Toast.LENGTH_SHORT).show()
    }
}
