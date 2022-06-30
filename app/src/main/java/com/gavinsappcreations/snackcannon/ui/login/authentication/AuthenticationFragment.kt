package com.gavinsappcreations.snackcannon.ui.login.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gavinsappcreations.snackcannon.R
import com.gavinsappcreations.snackcannon.databinding.FragmentAuthenticationBinding
import com.gavinsappcreations.snackcannon.util.showSoftKeyboard
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class AuthenticationFragment : Fragment() {

    private val viewModel by viewModels<AuthenticationViewModel>()
    private lateinit var binding: FragmentAuthenticationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_authentication, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.countryCodePicker.registerCarrierNumberEditText(binding.phoneNumberEditText)

        binding.phoneNumberEditText.doAfterTextChanged {
            viewModel.enableSubmitPhoneNumberButton(it)
            binding.phoneNumberEditText.error = null
        }

        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.eventFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { event ->
                when (event) {
                    is AuthenticationViewModel.AuthenticationEvent.DetectUserAlreadyLoggedInEvent ->
                        findNavController().navigate(
                            AuthenticationFragmentDirections.actionAuthenticationFragmentToStorefrontFragment()
                        )
                    is AuthenticationViewModel.AuthenticationEvent.VerifyPhoneNumberEvent ->
                        submitPhoneNumberToFirebaseForValidation()
                    is AuthenticationViewModel.AuthenticationEvent.InvalidPhoneNumberEvent ->
                        binding.phoneNumberEditText.error = getString(R.string.invalid_phone_number)
                    is AuthenticationViewModel.AuthenticationEvent.ErrorToastEvent ->
                        Toast.makeText(requireActivity(), event.errorText, Toast.LENGTH_SHORT).show()
                    is AuthenticationViewModel.AuthenticationEvent.NavigateToAddressDetailsFragmentEvent ->
                        findNavController().navigate(
                            AuthenticationFragmentDirections.actionAuthenticationFragmentToAddressDetailsFragment()
                        )
                    is AuthenticationViewModel.AuthenticationEvent.NavigateToExploreFragmentEvent ->
                        findNavController().navigate(
                            AuthenticationFragmentDirections.actionAuthenticationFragmentToStorefrontFragment()
                        )
                    is AuthenticationViewModel.AuthenticationEvent.SignInInWithCredentialEvent -> {
                        launch {
                            viewModel.signInWithPhoneAuthCredential(event.credential)
                        }
                        binding.codeEntryPinField.error = null
                    }
                    is AuthenticationViewModel.AuthenticationEvent.WrongCodeEnteredEvent ->
                        binding.codeEntryPinField.error = getString(R.string.wrong_code_entered)
                    is AuthenticationViewModel.AuthenticationEvent.ShowKeyboardForCodeEntryEvent ->
                        showKeyboard(binding.codeEntryPinField)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This lets us handle Back button presses from within the Fragment.
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!viewModel.viewState.value.isPhoneNumberLayoutVisible) {
                    // If phoneNumberGroup isn't currently visible, show it when pressing Back.
                    viewModel.showPhoneNumberGroupAndHideCodeEntryGroup()
                } else {
                    // Otherwise, just handle Back button press as normal.
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        })

        /**
         * Receive the value we pass through back stack from AddressDetailsFragment. This allows us
         * to reinitialize this Fragment with phoneNumberGroup visible and codeEntryPinField text empty.
         */
        setFragmentResultListener("backPressRequestKey") { _, bundle ->
            val result: Boolean = bundle.getBoolean("backPressed")
            // if result is true, this means we arrived here from a back press from AddressDetailsFragment.
            if (result) {
                viewModel.showPhoneNumberGroupAndHideCodeEntryGroup()
                binding.codeEntryPinField.setText("")
            }
        }
    }

    // Move this to ViewModel if Firebase ever provides a version that doesn't require Activity.
    private fun submitPhoneNumberToFirebaseForValidation() {
        val options = PhoneAuthOptions.newBuilder(viewModel.firebaseAuth)
            .setPhoneNumber(
                binding.countryCodePicker.selectedCountryCodeWithPlus
                        + binding.phoneNumberEditText.text.toString()
            )
            .setTimeout(20L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(viewModel.firebaseOnVerificationStateChangedCallbacks)
        viewModel.token?.let {
            options.setForceResendingToken(it)
        }
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }


    // For some reason we need to slightly delay showing the keyboard when showing it at Fragment start.
    private fun showKeyboard(view: View) {
        binding.root.postDelayed({
            showSoftKeyboard(view)
        }, 100)
    }
}

