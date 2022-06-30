package com.gavinsappcreations.snackcannon.ui.storefront.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gavinsappcreations.snackcannon.R
import com.gavinsappcreations.snackcannon.databinding.FragmentExploreBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {

    private val viewModel by viewModels<ExploreViewModel>()
    private lateinit var binding: FragmentExploreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_explore, container, false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.eventFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { event ->
                when (event) {
                    is ExploreViewModel.ExploreEvent.DisplayErrorToast ->
                        Toast.makeText(requireActivity(), event.errorText, Toast.LENGTH_LONG).show()
                    is ExploreViewModel.ExploreEvent.NavigateToCategoryEvent ->
                        findNavController().navigate(
                            ExploreFragmentDirections.actionExploreFragmentToSearchResultFragment(event.snackCategory)
                        )
                    is ExploreViewModel.ExploreEvent.DeliveryAddressChangedEvent ->
                        binding.deliveryAddressValueTextView.text = event.newAddress
                    is ExploreViewModel.ExploreEvent.NavigateToAddressDetailsFragmentEvent ->
                        findNavController().navigate(
                            ExploreFragmentDirections
                                .actionExploreFragmentToAddressDetailsFragmentInStorefront()
                        )
                }
            }
        }

    }


}