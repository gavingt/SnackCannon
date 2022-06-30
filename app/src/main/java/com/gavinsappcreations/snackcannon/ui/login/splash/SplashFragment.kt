package com.gavinsappcreations.snackcannon.ui.login.splash

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
import com.gavinsappcreations.snackcannon.databinding.FragmentSplashBinding
import com.gavinsappcreations.snackcannon.util.getNavBarHeight
import com.gavinsappcreations.snackcannon.util.getStatusBarHeight
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private val viewModel by viewModels<SplashViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentSplashBinding>(
            inflater, com.gavinsappcreations.snackcannon.R.layout.fragment_splash, container, false
        )

        // Adjust icon in splash screen so it doesn't shift upwards when Fragment loads.
        binding.splashImageView.translationY = (getNavBarHeight(resources) - getStatusBarHeight(resources)) / 2.0f

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.eventFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect {
                when (it) {
                    is SplashViewModel.SplashEvent.DisplayErrorToastEvent ->
                        Toast.makeText(requireActivity(), it.errorText, Toast.LENGTH_SHORT).show()
                    is SplashViewModel.SplashEvent.NavigateToAuthenticationFragmentEvent ->
                        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToAuthenticationFragment())
                    is SplashViewModel.SplashEvent.NavigateToExploreFragmentEvent ->
                        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToStorefrontFragment())
                }
            }
        }
    }
}



