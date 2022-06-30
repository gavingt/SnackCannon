package com.gavinsappcreations.snackcannon.ui.storefront.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gavinsappcreations.snackcannon.R
import com.gavinsappcreations.snackcannon.data.domain.SnackCategory
import com.gavinsappcreations.snackcannon.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private val viewModel by viewModels<SearchViewModel>()
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_search, container, false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.submitButton.setOnClickListener {
            findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToSearchResultFragment(SnackCategory.NONE, binding.searchEditText.text.toString()))
        }

        // TODO: change this: https://stackoverflow.com/a/48810268/7434090

        binding.searchEditText.setOnEditorActionListener { v, actionId, event ->
            findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToSearchResultFragment(SnackCategory.NONE, binding.searchEditText.text.toString()))
            true
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

/*        lifecycleScope.launch {
            viewModel.eventFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { event ->
                when (event) {
                    is ExploreViewModel.ExploreEvent.DisplayErrorToast ->
                        Toast.makeText(requireActivity(), event.errorText, Toast.LENGTH_LONG).show()
                    is ExploreViewModel.ExploreEvent.NavigateToCategoryEvent ->
                        Toast.makeText(requireActivity(), event.category.toString(), Toast.LENGTH_SHORT).show()
                    is ExploreViewModel.ExploreEvent.DeliveryAddressChangedEvent ->
                        binding.deliveryAddressValueTextView.text = event.newAddress
                    is ExploreViewModel.ExploreEvent.NavigateToAddressDetailsFragmentEvent ->
                        findNavController().navigate(
                            ExploreFragmentDirections
                            .actionExploreFragmentToAddressDetailsFragmentInStorefront())
                }
            }
        }*/

    }

}