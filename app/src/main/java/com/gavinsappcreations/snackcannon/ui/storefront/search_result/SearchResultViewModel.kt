package com.gavinsappcreations.snackcannon.ui.storefront.search_result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gavinsappcreations.snackcannon.App
import com.gavinsappcreations.snackcannon.data.domain.SnackCategory
import com.gavinsappcreations.snackcannon.data.network.NetworkSnack
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class SearchResultViewModel(
    private val snackCategory: SnackCategory,
    private val searchTerm: String?
) : ViewModel() {

    // All possible events for the Fragment.
    sealed class SearchResultEvent {
        object NavigateToAddressDetailsFragmentEvent : SearchResultEvent()
        class DeliveryAddressChangedEvent(val newAddress: String) : SearchResultEvent()
        class NavigateToCategoryEvent(val category: SnackCategory) : SearchResultEvent()
        class DisplayErrorToast(val errorText: String) : SearchResultEvent()
    }

    // Holds current state of the Fragment.
    data class SearchResultState(
        var isTestButtonActive: Boolean? = null
    )


    private val snackRepository = App.snackRepository

    // Channel for sending one-off events from viewModel to Fragment.
    private val _eventChannel = Channel<SearchResultEvent>(Channel.UNLIMITED)

    // Turn eventChannel into a Flow that the Fragment can use to receive events.
    val eventFlow = _eventChannel.receiveAsFlow()

    // Private mutable Flow representing the state of the Fragment.
    private val _viewState = MutableStateFlow(SearchResultState())

    // Public non-mutable Flow representing the state of the Fragment.
    val viewState = _viewState.asStateFlow()

    val allSnacks: Flow<PagingData<NetworkSnack>> =
        snackRepository.getSnackList(snackCategory, searchTerm).cachedIn(viewModelScope)


    // Factory for constructing SnackResultViewModel with snackCategory parameter.
    class Factory(
        private val snackCategory: SnackCategory,
        private val searchTerm: String?
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchResultViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchResultViewModel(
                    snackCategory, searchTerm
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}