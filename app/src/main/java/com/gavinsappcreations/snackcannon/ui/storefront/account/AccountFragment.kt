package com.gavinsappcreations.snackcannon.ui.storefront.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gavinsappcreations.snackcannon.R
import com.gavinsappcreations.snackcannon.databinding.FragmentAccountBinding
import com.gavinsappcreations.snackcannon.databinding.FragmentExploreBinding
import com.gavinsappcreations.snackcannon.ui.storefront.explore.ExploreViewModel

class AccountFragment : Fragment() {

    //private val viewModel by viewModels<AccountViewModel>()
    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_account, container, false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        //binding.viewModel = viewModel

        binding.changeAddressButton.setOnClickListener {
            findNavController().navigate(AccountFragmentDirections.actionAccountFragmentToAddressDetailsFragment())
        }

        binding.changeContactInfoButton.setOnClickListener {
            findNavController().navigate(AccountFragmentDirections.actionAccountFragmentToContactInfoFragment())
        }

        return binding.root
    }
}