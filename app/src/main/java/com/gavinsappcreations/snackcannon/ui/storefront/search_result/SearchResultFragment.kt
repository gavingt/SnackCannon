package com.gavinsappcreations.snackcannon.ui.storefront.search_result

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gavinsappcreations.snackcannon.R
import com.gavinsappcreations.snackcannon.databinding.FragmentSearchResultBinding
import com.gavinsappcreations.snackcannon.ui.storefront.explore.NetworkSnackAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchResultFragment : Fragment() {

    private val viewModel by viewModels<SearchResultViewModel>{
        SearchResultViewModel.Factory(
            SearchResultFragmentArgs.fromBundle(requireArguments()).snackCategory,
            SearchResultFragmentArgs.fromBundle(requireArguments()).searchTerm
            )
    }
    private lateinit var binding: FragmentSearchResultBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment.
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_search_result, container, false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val networkSnackAdapter = NetworkSnackAdapter()
        binding.recyclerView.adapter = networkSnackAdapter
        lifecycleScope.launch {
            viewModel.allSnacks.collectLatest {
                networkSnackAdapter.submitData(it)
            }
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