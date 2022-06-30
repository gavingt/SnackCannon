package com.gavinsappcreations.snackcannon.ui.login.contact_info

import android.os.Parcelable
import android.util.Patterns
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gavinsappcreations.snackcannon.App
import com.gavinsappcreations.snackcannon.R
import com.gavinsappcreations.snackcannon.data.datastore.DataStoreKeys
import com.gavinsappcreations.snackcannon.data.domain.Profile
import com.gavinsappcreations.snackcannon.data.domain.State
import com.gavinsappcreations.snackcannon.util.KEY_SAVED_STATE_ADDRESS_DETAILS_VIEW_MODEL
import com.gavinsappcreations.snackcannon.util.KEY_SAVED_STATE_CONTACT_INFO_VIEW_MODEL
import com.gavinsappcreations.snackcannon.util.isEmailAddressValid
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize


class ContactInfoViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    // All possible events for the Fragment.
    sealed class ContactInfoEvent {
        object NavigateToStorefrontFragmentEvent : ContactInfoEvent()
        object PopBackStackEvent : ContactInfoEvent()
        class DisplayErrorToast(val errorText: String) : ContactInfoEvent()
    }

    // Holds current state of the Fragment.
    @Parcelize
    data class ContactInfoState(
        var fullName: String? = null,
        var emailAddress: String? = null,
        var emailAddressErrorTextResource: Int? = null,
        var isSubmitButtonEnabled: Boolean = false
    ): Parcelable


    private val repository = App.profileRepository
    private var isUserInLoginFlow = true

    // Channel for sending one-off events from viewModel to Fragment.
    private val _eventChannel = Channel<ContactInfoEvent>(Channel.UNLIMITED)

    // Turn eventChannel into a Flow that the Fragment can use to receive events.
    val eventFlow = _eventChannel.receiveAsFlow()

    // Private mutable Flow representing the state of the Fragment.
    private val _viewState = MutableStateFlow(ContactInfoState())

    // Public non-mutable Flow representing the state of the Fragment.
    val viewState = _viewState.asStateFlow()

    private fun displayErrorToast(errorText: String) = viewModelScope.launch {
        _eventChannel.send(ContactInfoEvent.DisplayErrorToast(errorText))
    }


    init {
        /**
         * If process death occurred here, savedViewState will be non-null and
         * we'll use it to restore _viewState.value. Otherwise, we'll get the value from DataStore.
         */
        val savedViewState = savedStateHandle.get<ContactInfoState>(KEY_SAVED_STATE_CONTACT_INFO_VIEW_MODEL)
        if (savedViewState != null) {
            _viewState.value = savedViewState
        } else {
            viewModelScope.launch {
                // Get the first emitted value from profileFromDataStoreFlow and use it to initialize _viewState.
                repository.profileFromDataStoreFlow.first {
                    _viewState.value.fullName = it.fullName
                    _viewState.value.emailAddress = it.emailAddress
                    true
                }
            }
        }
    }


    fun onFullNameChanged(newName: String?) {
        _viewState.update {
            it.copy(
                fullName = newName.toString(),
                isSubmitButtonEnabled = !newName.isNullOrEmpty() && !viewState.value.emailAddress.isNullOrEmpty()
            )
        }
    }

    fun onEmailAddressChanged(newEmailAddress: String?) {
        _viewState.update {
            it.copy(
                emailAddress = newEmailAddress.toString(),
                isSubmitButtonEnabled = !viewState.value.fullName.isNullOrEmpty() && !newEmailAddress.isNullOrEmpty()
            )
        }
    }


    fun onSubmitButtonClicked() {
        when (isEmailAddressValid(viewState.value.emailAddress)) {
            true -> {
                _viewState.update { it.copy(emailAddressErrorTextResource = null) }
                writeProfileToFirestore()
            }
            false -> {
                _viewState.update { it.copy(emailAddressErrorTextResource = R.string.enter_a_valid_email_address) }
            }
        }
    }




    private fun writeProfileToFirestore() = viewModelScope.launch {
        when (val writeResult = repository.writeProfileToFirestore(createPreferences())) {
            is State.Success -> {
                when (isUserInLoginFlow) {
                    true -> _eventChannel.send(ContactInfoEvent.NavigateToStorefrontFragmentEvent)
                    false -> _eventChannel.send(ContactInfoEvent.PopBackStackEvent)
                }
            }
            is State.Failed ->
                displayErrorToast(writeResult.throwable.message ?: "Failed to write profile")
        }
    }


    private fun createPreferences(): MutablePreferences {
        val prefs = mutablePreferencesOf()
        prefs[DataStoreKeys.FULL_NAME] = _viewState.value.fullName!!
        prefs[DataStoreKeys.EMAIL_ADDRESS] = _viewState.value.emailAddress!!
        return prefs
    }


    fun onReceiveArgs(isUserInLoginFlow: Boolean) {
        this.isUserInLoginFlow = isUserInLoginFlow
    }


    fun onSaveInstanceState() {
        savedStateHandle.set(KEY_SAVED_STATE_CONTACT_INFO_VIEW_MODEL, _viewState.value)
    }

}