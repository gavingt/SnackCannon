package com.gavinsappcreations.snackcannon.ui.storefront.search

import android.view.SearchEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gavinsappcreations.snackcannon.App
import com.gavinsappcreations.snackcannon.data.network.NetworkSnack
import com.gavinsappcreations.snackcannon.ui.storefront.explore.ExploreViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class SearchViewModel : ViewModel() {

    // All possible events for the Fragment.
/*    sealed class SearchEvent {
        object NavigateToAddressDetailsFragmentEvent : SearchEvent.ExploreEvent()
        class DeliveryAddressChangedEvent (val newAddress: String) : SearchEvent.ExploreEvent()
        class NavigateToCategoryEvent(val category: SnackCategories) : SearchEvent.ExploreEvent()
        class DisplayErrorToast(val errorText: String) : SearchEvent.ExploreEvent()
    }*/

    // Holds current state of the Fragment.
    data class SearchState(
        var deliveryAddress: String? = null)


    private val snackRepository = App.snackRepository

    // Channel for sending one-off events from viewModel to Fragment.
    private val _eventChannel = Channel<SearchEvent>(Channel.UNLIMITED)

    // Turn eventChannel into a Flow that the Fragment can use to receive events.
    val eventFlow = _eventChannel.receiveAsFlow()

    // Private mutable Flow representing the state of the Fragment.
    private val _viewState = MutableStateFlow(SearchState())

    // Public non-mutable Flow representing the state of the Fragment.
    val viewState = _viewState.asStateFlow()


    //val allSnacks: Flow<PagingData<NetworkSnack>> = snackRepository.allSnacks.cachedIn(viewModelScope)


}