package com.gavinsappcreations.snackcannon.ui.login.address_details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gavinsappcreations.snackcannon.R
import com.gavinsappcreations.snackcannon.databinding.FragmentAddressDetailsBinding
import com.gavinsappcreations.snackcannon.util.KEY_SAVED_STATE_ADDRESS_DETAILS_VIEW_MODEL
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class AddressDetailsFragment : Fragment() {

    companion object {
        private val TAG = AddressDetailsFragment::class.qualifiedName
    }

    private val viewModel by viewModels<AddressDetailsViewModel>()
    private lateinit var binding: FragmentAddressDetailsBinding
    private val args: AddressDetailsFragmentArgs by navArgs()
    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_address_details, container, false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        // Pass isUserInLoginFlow from args to viewModel, to indicate if we're here from login flow or not.
        viewModel.onReceiveArgs(args.isUserInLoginFlow)

        /**
         * This bundle will be passed back to LoginFragment if user pops the back stack from here.
         * This will allow LoginFragment to reinitialize itself back to phone number
         * entry instead of code entry.
         */
        setFragmentResult("backPressRequestKey", bundleOf("backPressed" to true))

        initializeAutocompleteFragment()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Listen for events from viewModel.
        lifecycleScope.launch {
            viewModel.eventFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { event ->
                when (event) {
                    is AddressDetailsViewModel.AddressDetailsEvent.ShowAutocompleteFragmentEvent -> {
                        autocompleteFragment.showFragment()
                        requireActivity().currentFocus?.clearFocus()
                    }
                    is AddressDetailsViewModel.AddressDetailsEvent.NavigateToContactInfoFragmentEvent ->
                        findNavController().navigate(
                            AddressDetailsFragmentDirections.actionAddressDetailsFragmentToContactInfoFragment()
                        )
                    is AddressDetailsViewModel.AddressDetailsEvent.PopBackStackEvent -> {
                        findNavController().popBackStack()
                    }
                    is AddressDetailsViewModel.AddressDetailsEvent.LogErrorEvent ->
                        Log.d(TAG, event.errorText)
                }
            }
        }
    }


    private fun initializeAutocompleteFragment() {
        Places.initialize(requireActivity(), getString(R.string.google_places_api_key))

        autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleteFragment.setTypeFilter(TypeFilter.ADDRESS)
        autocompleteFragment.setHint("Delivery address")

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                viewModel.onAddressSelected(place.address!!, place.latLng!!)
            }

            override fun onError(status: Status) {
                viewModel.logErrorMessage(getString(R.string.address_autocomplete_error, status))
            }
        })
    }


    /**
     * We only enable hint animations after the text has been initialized, as this avoids
     * a jarring animation when entering the Fragment.
     */
    override fun onResume() {
        super.onResume()
        binding.unitNumberTextInputLayout.isHintAnimationEnabled = true
        binding.deliveryNotesTextInputLayout.isHintAnimationEnabled = true
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.onSaveInstanceState()
    }

    /**
     * Show autocompleteFragment programmatically, so we can invoke it from our own View rather
     * than using the pre-supplied one.
     */
    private fun AutocompleteSupportFragment.showFragment() {
        setText("")
        requireView().post {
            requireView().findViewById<View>(R.id.places_autocomplete_search_input)
                .performClick()
        }
    }

}