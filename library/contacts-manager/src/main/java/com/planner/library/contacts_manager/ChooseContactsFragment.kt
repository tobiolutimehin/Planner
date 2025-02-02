package com.planner.library.contacts_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.planner.library.contacts_manager.databinding.FragmentChooseContactsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChooseContactsFragment : Fragment() {
    private lateinit var _binding: FragmentChooseContactsBinding
    val binding get() = _binding

    private lateinit var adapter: ContactListRecyclerAdapter

    @Inject lateinit var contactFetcher: ContactFetcher

    private val arguments: ChooseContactsFragmentArgs by navArgs()

    private val chooseContactsViewModel: ChooseContactsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChooseContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Pass the initial contacts to the ViewModel
        val contacts = arguments.selectedContacts
        chooseContactsViewModel.setSelectedContacts(contacts?.toList() ?: emptyList())

        // Initialize the adapter with the selected contacts from the ViewModel
        adapter = ContactListRecyclerAdapter(
            selectedContacts = chooseContactsViewModel.selectedContacts.value ?: mutableSetOf(),
            onContactSelected = { contact, isSelected ->
                chooseContactsViewModel.toggleContactSelection(contact, isSelected)
            }
        )

        binding.chooseContactsRecyclerView.adapter = adapter

        // Observe the list of all contacts from the ViewModel and update the adapter
        chooseContactsViewModel.allContacts.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                adapter.submitList(it)
            }
        }

        binding.chooseContactsDoneButton.setOnClickListener {
            confirmSelection()
        }
    }

    // Reset the selection when fragment is resumed
    override fun onResume() {
        chooseContactsViewModel.resetSelection()
        super.onResume()
    }

    // Confirm selection and send back the selected contacts
    private fun confirmSelection() {
        val selectedContacts = chooseContactsViewModel.selectedContacts.value?.toList() ?: emptyList()

        val bundle = Bundle().apply {
            putParcelableArrayList("selected_contacts", ArrayList(selectedContacts))
        }

        setFragmentResult("contactSelectionResult", bundle)

        // Navigate back
        findNavController().popBackStack()
    }
}
