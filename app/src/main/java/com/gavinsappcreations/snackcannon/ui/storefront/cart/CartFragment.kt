package com.gavinsappcreations.snackcannon.ui.storefront.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gavinsappcreations.snackcannon.R
import com.gavinsappcreations.snackcannon.databinding.FragmentCartBinding


class CartFragment : Fragment() {

    //private val viewModel by viewModels<CartViewModel>()
    private lateinit var binding: FragmentCartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_cart, container, false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        //binding.viewModel = viewModel

        return binding.root
    }

}


