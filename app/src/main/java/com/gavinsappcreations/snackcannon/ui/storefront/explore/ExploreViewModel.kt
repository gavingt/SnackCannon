package com.gavinsappcreations.snackcannon.ui.storefront.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gavinsappcreations.snackcannon.App
import com.gavinsappcreations.snackcannon.data.domain.SnackCategory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {

    // All possible events for the Fragment.
    sealed class ExploreEvent {
        object NavigateToAddressDetailsFragmentEvent : ExploreEvent()
        class DeliveryAddressChangedEvent (val newAddress: String) : ExploreEvent()
        class NavigateToCategoryEvent(val snackCategory: SnackCategory) : ExploreEvent()
        class DisplayErrorToast(val errorText: String) : ExploreEvent()
    }

    // Holds current state of the Fragment.
    data class ExploreState(
        var deliveryAddress: String? = null)


    private val profileRepository = App.profileRepository
    private val snackRepository = App.snackRepository

    // Channel for sending one-off events from viewModel to Fragment.
    private val _eventChannel = Channel<ExploreEvent>(Channel.UNLIMITED)

    // Turn eventChannel into a Flow that the Fragment can use to receive events.
    val eventFlow = _eventChannel.receiveAsFlow()

    // Private mutable Flow representing the state of the Fragment.
    private val _viewState = MutableStateFlow(ExploreState())

    // Public non-mutable Flow representing the state of the Fragment.
    val viewState = _viewState.asStateFlow()


    //val allSnacks: Flow<PagingData<NetworkSnack>> = snackRepository.allSnacks.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            profileRepository.profileFromDataStoreFlow.collect { newProfile ->
                newProfile.let {
                    _viewState.update { it.copy(deliveryAddress = newProfile.deliveryAddress) }
                }
            }
        }
    }

    private fun displayErrorToast(errorText: String) = viewModelScope.launch {
        _eventChannel.send(ExploreEvent.DisplayErrorToast(errorText))
    }

    fun onNavigateToCategory(category: SnackCategory) = viewModelScope.launch {
        _eventChannel.send(ExploreEvent.NavigateToCategoryEvent(category))
    }

    fun onDeliveryAddressClicked() = viewModelScope.launch {
        _eventChannel.send(ExploreEvent.NavigateToAddressDetailsFragmentEvent)
    }

}