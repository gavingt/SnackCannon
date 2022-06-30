package com.gavinsappcreations.snackcannon.ui.login.authentication

import android.os.SystemClock
import android.text.Editable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gavinsappcreations.snackcannon.App
import com.gavinsappcreations.snackcannon.data.domain.State
import com.gavinsappcreations.snackcannon.util.CODE_RESET_TIMEOUT_IN_SECONDS
import com.gavinsappcreations.snackcannon.util.ONE_SECOND_IN_MILLISECONDS
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class AuthenticationViewModel : ViewModel() {

    // All possible events for the Fragment.
    sealed class AuthenticationEvent {
        object DetectUserAlreadyLoggedInEvent : AuthenticationEvent()
        object VerifyPhoneNumberEvent : AuthenticationEvent()
        object InvalidPhoneNumberEvent : AuthenticationEvent()
        object NavigateToAddressDetailsFragmentEvent : AuthenticationEvent()
        object NavigateToExploreFragmentEvent : AuthenticationEvent()
        object ShowKeyboardForCodeEntryEvent : AuthenticationEvent()
        object WrongCodeEnteredEvent : AuthenticationEvent()
        class ErrorToastEvent(val errorText: String) : AuthenticationEvent()
        class SignInInWithCredentialEvent(val credential: PhoneAuthCredential) : AuthenticationEvent()
    }

    // Holds current state of the Fragment.
    data class LoginState(
        var isSubmitPhoneNumberButtonEnabled: Boolean = false,
        var isResendCodeButtonEnabled: Boolean = false,
        var isSubmitCodeButtonEnabled: Boolean = false,
        var isProgressBarVisible: Boolean = false,
        var isPhoneNumberLayoutVisible: Boolean = true,
        var phoneNumber: String = "",
        var codeResendTimeRemainingInSeconds: Long = 20
    )

    private val repository = App.profileRepository
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val firebaseOnVerificationStateChangedCallbacks = createFirebaseOnVerificationStateChangedCallbacks()
    lateinit var verificationId: String
    var token: PhoneAuthProvider.ForceResendingToken? = null
    private var resendCodeTimer: Timer? = null

    // Channel for sending one-off events from viewModel to Fragment.
    private val _eventChannel = Channel<AuthenticationEvent>(Channel.UNLIMITED)

    // Turn eventChannel into a Flow that the Fragment can use to receive events.
    val eventFlow = _eventChannel.receiveAsFlow()

    // Private mutable Flow representing the state of the Fragment.
    private val _viewState = MutableStateFlow(LoginState())

    // Public non-mutable Flow representing the state of the Fragment.
    val viewState = _viewState.asStateFlow()


    fun verifyPhoneNumber() = viewModelScope.launch {
        _viewState.update { it.copy(isProgressBarVisible = true) }
        _eventChannel.send(AuthenticationEvent.VerifyPhoneNumberEvent)
    }

    fun enableSubmitPhoneNumberButton(text: Editable?) {
        _viewState.update { it.copy(isSubmitPhoneNumberButtonEnabled = text?.isNotEmpty() == true) }
    }

    fun displayErrorToast(errorText: String) = viewModelScope.launch {
        _eventChannel.send(AuthenticationEvent.ErrorToastEvent(errorText))
    }

    private fun onWrongCodeEntered() = viewModelScope.launch {
        _eventChannel.send(AuthenticationEvent.WrongCodeEnteredEvent)
    }

    fun enableSubmitCodeButton(text: Editable?) {
        _viewState.update { it.copy(isSubmitCodeButtonEnabled = text?.length == 6) }
    }

    fun onSubmitCodeButtonClick(codeEntered: String) = viewModelScope.launch {
        val credential = PhoneAuthProvider.getCredential(verificationId, codeEntered)
        _eventChannel.send(AuthenticationEvent.SignInInWithCredentialEvent(credential))
    }

    fun didNotReceiveCodeButtonPressed() = viewModelScope.launch {
        _eventChannel.send(AuthenticationEvent.VerifyPhoneNumberEvent)
    }

    fun showKeyboardForCodeEntry() = viewModelScope.launch {
        _eventChannel.send(AuthenticationEvent.ShowKeyboardForCodeEntryEvent)
    }

    fun onInstantVerificationOccurred(credential: PhoneAuthCredential) = viewModelScope.launch {
        _eventChannel.send(AuthenticationEvent.SignInInWithCredentialEvent(credential))
    }

    fun showPhoneNumberGroupAndHideCodeEntryGroup() {
        _viewState.update { it.copy(isPhoneNumberLayoutVisible = true) }
    }

    private fun navigateToAddressDetailsFragment() = viewModelScope.launch {
        _eventChannel.send(AuthenticationEvent.NavigateToAddressDetailsFragmentEvent)
    }

    private fun navigateToExploreFragment() = viewModelScope.launch {
        _eventChannel.send(AuthenticationEvent.NavigateToExploreFragmentEvent)
    }


    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        when (val authState = repository.signInWithPhoneAuthCredential(credential)) {
            is State.Success ->
                viewModelScope.launch {
                    readProfileFromFirestore()
                }
            is State.Failed -> {
                if (authState.throwable is FirebaseAuthInvalidCredentialsException) {
                    onWrongCodeEntered()
                } else {
                    displayErrorToast("Something went wrong while signing in")
                }
            }
        }
    }


    // Download profile from Firestore and update the repository's cached profile.
    private suspend fun readProfileFromFirestore() {
        when (val profileState = repository.readProfileFromFirestore()) {
            is State.Success -> {
                val profile = profileState.data
                if (profile != null && repository.isProfileComplete(profile)) {
                    navigateToExploreFragment()
                } else {
                    navigateToAddressDetailsFragment()
                }
            }
            is State.Failed -> {
                // Error occurred while getting profile, so just inform user and go to AddressDetailsFragment.
                displayErrorToast(profileState.throwable.message ?: "Failed to get profile")
                navigateToAddressDetailsFragment()
            }
            is State.Loading ->
                return
        }
    }


    private fun createFirebaseOnVerificationStateChangedCallbacks() =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                // The SMS verification code has been sent to the provided phone number.
                this@AuthenticationViewModel.verificationId = verificationId
                this@AuthenticationViewModel.token = token
                startResendCodeTimer()
                showKeyboardForCodeEntry()
                _viewState.update { it.copy(isProgressBarVisible = false, isPhoneNumberLayoutVisible = false) }
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Instant verification or auto-retrieval of SMS code has occurred.
                _viewState.update { it.copy(isProgressBarVisible = false) }
                onInstantVerificationOccurred(credential)
            }

            // Invalid request for verification has been made.
            override fun onVerificationFailed(e: FirebaseException) {
                _viewState.update { it.copy(isProgressBarVisible = false) }
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid phone number.
                    viewModelScope.launch {
                        _eventChannel.send(AuthenticationEvent.InvalidPhoneNumberEvent)
                    }
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded.
                    displayErrorToast("Quota exceeded")
                }
            }
        }


    /**
     * Start a timer that counts down from [CODE_RESET_TIMEOUT_IN_SECONDS] in increments of [ONE_SECOND_IN_MILLISECONDS].
     */
    private fun startResendCodeTimer() {
        resendCodeTimer = Timer()
        val initialTime = SystemClock.elapsedRealtime()

        resendCodeTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val elapsedTimeInSeconds: Long = (SystemClock.elapsedRealtime() - initialTime) / ONE_SECOND_IN_MILLISECONDS
                val hasCodeResetTimeoutElapsed = elapsedTimeInSeconds == CODE_RESET_TIMEOUT_IN_SECONDS
                if (hasCodeResetTimeoutElapsed) {
                    resendCodeTimer?.cancel()
                }
                _viewState.update {
                    it.copy(
                        codeResendTimeRemainingInSeconds = CODE_RESET_TIMEOUT_IN_SECONDS - elapsedTimeInSeconds,
                        isResendCodeButtonEnabled = hasCodeResetTimeoutElapsed
                    )
                }
            }
        }, ONE_SECOND_IN_MILLISECONDS, ONE_SECOND_IN_MILLISECONDS)
    }

    override fun onCleared() {
        super.onCleared()
        resendCodeTimer?.cancel()
    }

}