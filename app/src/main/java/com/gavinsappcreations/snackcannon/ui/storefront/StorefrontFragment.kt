package com.gavinsappcreations.snackcannon.ui.storefront

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.gavinsappcreations.snackcannon.R
import com.gavinsappcreations.snackcannon.databinding.FragmentStorefrontBinding


/**
 * Fragment that houses the BottomNavigationView and serves as the host of the
 * storefront_navigation navgraph.
 */
class StorefrontFragment : Fragment() {

    //private val viewModel by viewModels<StorefrontViewModel>()
    private lateinit var binding: FragmentStorefrontBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_storefront, container, false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        //binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpBottomNavigationBar()
    }

    private fun setUpBottomNavigationBar() {
        val nestedNavHostFragment = childFragmentManager.findFragmentById(R.id.storefront_nav_host_fragment) as? NavHostFragment
        val navController = nestedNavHostFragment!!.navController
        val bottomNavigationView = binding.bottomNavigationView
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
        bottomNavigationView.setOnItemReselectedListener {
            // If user clicks on the tab that's already selected, return to first screen of that tab.
            navController.popBackStack(it.itemId, false)
        }
    }
}


