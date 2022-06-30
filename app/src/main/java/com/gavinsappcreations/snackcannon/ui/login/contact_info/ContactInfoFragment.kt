package com.gavinsappcreations.snackcannon.ui.login.contact_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gavinsappcreations.snackcannon.R
import com.gavinsappcreations.snackcannon.databinding.FragmentContactInfoBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ContactInfoFragment : Fragment() {

    private val viewModel by viewModels<ContactInfoViewModel>()
    private lateinit var binding: FragmentContactInfoBinding
    private val args: ContactInfoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_contact_info, container, false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.fullNameTextInputEditText.doAfterTextChanged {
            viewModel.onFullNameChanged(it.toString())
        }

        binding.emailAddressTextInputEditText.doAfterTextChanged {
            viewModel.onEmailAddressChanged(it.toString())
        }

        // Pass isUserInLoginFlow from args to viewModel, to indicate if we're here from login flow or not.
        viewModel.onReceiveArgs(args.isUserInLoginFlow)

        binding.emailAddressTextInputEditText.error

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.eventFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { event ->
                when (event) {
                    is ContactInfoViewModel.ContactInfoEvent.NavigateToStorefrontFragmentEvent ->
                        findNavController().navigate(
                            ContactInfoFragmentDirections.actionContactInfoFragmentToStorefrontFragment()
                        )
                    is ContactInfoViewModel.ContactInfoEvent.DisplayErrorToast ->
                        Toast.makeText(requireActivity(), event.errorText, Toast.LENGTH_LONG).show()
                    is ContactInfoViewModel.ContactInfoEvent.PopBackStackEvent ->
                        findNavController().popBackStack()
                }
            }
        }
    }


    /**
     * We only enable hint animations after the text has been initialized, as this avoids
     * a jarring animation when entering the Fragment.
     */
    override fun onResume() {
        super.onResume()
        binding.emailAddressTextInputLayout.isHintAnimationEnabled = true
        binding.fullNameTextInputLayout.isHintAnimationEnabled = true
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.onSaveInstanceState()
    }

}