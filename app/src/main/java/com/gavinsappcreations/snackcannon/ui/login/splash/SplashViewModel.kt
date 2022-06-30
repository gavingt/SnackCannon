package com.gavinsappcreations.snackcannon.ui.login.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gavinsappcreations.snackcannon.App
import com.gavinsappcreations.snackcannon.data.domain.State
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    // All possible events for the Fragment.
    sealed class SplashEvent {
        object NavigateToAuthenticationFragmentEvent : SplashEvent()
        object NavigateToExploreFragmentEvent : SplashEvent()
        class DisplayErrorToastEvent(val errorText: String) : SplashEvent()
    }

    private val repository = App.profileRepository

    // Channel for sending one-off events from viewModel to Fragment.
    private val _eventChannel = Channel<SplashEvent>(Channel.UNLIMITED)

    // Turn eventChannel into a Flow that the Fragment can use to receive events.
    val eventFlow = _eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val userLoggedIn: Boolean? = repository.userLoginStatusChangedFlow.first()
            if (userLoggedIn == true) {
                readProfileFromFirestore()
            } else {
                navigateToAuthenticationFragment()
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
                    navigateToAuthenticationFragment()
                }
            }
            is State.Failed -> {
                // Error occurred while getting profile, so just inform user and go to AuthenticationFragment.
                displayErrorToast(profileState.throwable.message ?: "Failed to get profile")
                navigateToAuthenticationFragment()
            }
            is State.Loading ->
                return
        }
    }


    private fun navigateToAuthenticationFragment() = viewModelScope.launch {
        _eventChannel.send(SplashEvent.NavigateToAuthenticationFragmentEvent)
    }

    private fun navigateToExploreFragment() = viewModelScope.launch {
        _eventChannel.send(SplashEvent.NavigateToExploreFragmentEvent)
    }

    private fun displayErrorToast(errorText: String) = viewModelScope.launch {
        _eventChannel.send(SplashEvent.DisplayErrorToastEvent(errorText))
    }

}
