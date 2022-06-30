package com.gavinsappcreations.snackcannon.ui.login.address_details

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gavinsappcreations.snackcannon.App
import com.gavinsappcreations.snackcannon.data.datastore.DataStoreKeys
import com.gavinsappcreations.snackcannon.data.domain.Profile
import com.gavinsappcreations.snackcannon.data.domain.State
import com.gavinsappcreations.snackcannon.util.KEY_SAVED_STATE_ADDRESS_DETAILS_VIEW_MODEL
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddressDetailsViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    // All possible events for the Fragment.
    sealed class AddressDetailsEvent {
        object ShowAutocompleteFragmentEvent : AddressDetailsEvent()
        object PopBackStackEvent : AddressDetailsEvent()
        object NavigateToContactInfoFragmentEvent : AddressDetailsEvent()
        class LogErrorEvent(val errorText: String) : AddressDetailsEvent()
        class DisplayErrorToast(val errorText: String) : AddressDetailsEvent()
    }

    private var isUserInLoginFlow = true
    private val repository = App.profileRepository

    // Channel for sending one-off events from viewModel to Fragment.
    private val _eventChannel = Channel<AddressDetailsEvent>(Channel.UNLIMITED)

    // Turn eventChannel into a Flow that the Fragment can use to receive events.
    val eventFlow = _eventChannel.receiveAsFlow()

    // Private mutable Flow representing the state of the Fragment.
    private val _viewState = MutableStateFlow(Profile())

    // Public non-mutable Flow representing the state of the Fragment.
    val viewState = _viewState.asStateFlow()

    init {
        /**
         * If process death occurred here, savedViewState will be non-null and
         * we'll use it to restore _viewState.value. Otherwise, we'll get the value from DataStore.
         */
        val savedViewState = savedStateHandle.get<Profile>(KEY_SAVED_STATE_ADDRESS_DETAILS_VIEW_MODEL)
        if (savedViewState != null) {
            _viewState.value = savedViewState
        } else {
            viewModelScope.launch {
                // Copies the first emitted value from profileFromDataStoreFlow to _viewState.
                repository.profileFromDataStoreFlow.first {
                    _viewState.value = it.copy()
                    true
                }
            }
        }
    }


    fun showAutocompleteFragment() = viewModelScope.launch {
        _eventChannel.send(AddressDetailsEvent.ShowAutocompleteFragmentEvent)
    }

    fun onAddressSelected(address: String, latLng: LatLng) {
        _viewState.update {
            it.copy(
                deliveryAddress = address,
                addressLatitude = latLng.latitude,
                addressLongitude = latLng.longitude
            )
        }
    }


    fun onReceiveArgs(isUserInLoginFlow: Boolean) {
        this.isUserInLoginFlow = isUserInLoginFlow
    }

    fun logErrorMessage(errorText: String) = viewModelScope.launch {
        _eventChannel.send(AddressDetailsEvent.LogErrorEvent(errorText))
    }

    private fun displayErrorToast(errorText: String) = viewModelScope.launch {
        _eventChannel.send(AddressDetailsEvent.DisplayErrorToast(errorText))
    }


    fun writeProfileToFirestore() = viewModelScope.launch {
        when (val writeResult = repository.writeProfileToFirestore(createPreferences())) {
            is State.Success -> {
                when (isUserInLoginFlow) {
                    true -> _eventChannel.send(AddressDetailsEvent.NavigateToContactInfoFragmentEvent)
                    false -> _eventChannel.send(AddressDetailsEvent.PopBackStackEvent)
                }
            }
            is State.Failed ->
                displayErrorToast(writeResult.throwable.message ?: "Failed to write profile")
        }
    }


    private fun createPreferences(): MutablePreferences {
        val prefs = mutablePreferencesOf()
        prefs[DataStoreKeys.DELIVERY_ADDRESS] = _viewState.value.deliveryAddress!!
        prefs[DataStoreKeys.ADDRESS_LATITUDE] = _viewState.value.addressLatitude!!
        prefs[DataStoreKeys.ADDRESS_LONGITUDE] = _viewState.value.addressLongitude!!
        prefs[DataStoreKeys.UNIT_NUMBER] = _viewState.value.unitNumber ?: ""
        prefs[DataStoreKeys.DELIVERY_NOTES] = _viewState.value.deliveryNotes ?: ""
        return prefs
    }


    fun onSaveInstanceState() {
        savedStateHandle.set(KEY_SAVED_STATE_ADDRESS_DETAILS_VIEW_MODEL, _viewState.value)
    }

}