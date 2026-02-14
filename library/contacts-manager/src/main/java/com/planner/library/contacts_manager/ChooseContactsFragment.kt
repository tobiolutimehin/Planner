package com.planner.library.contacts_manager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.planner.library.contacts_manager.databinding.FragmentChooseContactsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChooseContactsFragment : Fragment() {
    private var _binding: FragmentChooseContactsBinding? = null
    private val binding get() = _binding!!

    private val selectedContactIds = mutableSetOf<Long>()
    private var contacts: List<PickerContact> = emptyList()

    private val preselectedContactIds: LongArray
        get() = arguments?.getLongArray(ContactPickerArgs.PRESELECTED_CONTACT_IDS) ?: LongArray(0)

    private val resultKey: String
        get() = arguments?.getString(ContactPickerArgs.RESULT_KEY).orEmpty()

    private lateinit var adapter: ContactListRecyclerAdapter

    @Inject
    lateinit var contactFetcher: ContactFetcher

    private val requestContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                loadContacts()
            } else {
                adapter.submitList(emptyList())
                binding.emptyState.visibility = View.VISIBLE
                binding.doneButton.isEnabled = false
            }
        }

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

        selectedContactIds.clear()
        selectedContactIds.addAll(preselectedContactIds.toSet())

        adapter =
            ContactListRecyclerAdapter { contact, isSelected ->
                if (isSelected) {
                    selectedContactIds.add(contact.id)
                } else {
                    selectedContactIds.remove(contact.id)
                }
                updateDoneButtonState()
            }

        binding.chooseContactsRecyclerView.adapter = adapter
        adapter.setSelectedContactIds(selectedContactIds)

        binding.doneButton.setOnClickListener { finishSelection() }
        binding.emptyState.visibility = View.GONE

        ensurePermissionAndLoadContacts()
        updateDoneButtonState()
    }

    private fun ensurePermissionAndLoadContacts() {
        val isGranted =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS,
            ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            loadContacts()
            return
        }

        requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun loadContacts() {
        lifecycleScope.launch {
            contacts = contactFetcher.fetchContactList().toList()
            adapter.submitList(contacts)
            adapter.setSelectedContactIds(selectedContactIds)
            binding.emptyState.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
            binding.doneButton.isEnabled = true
        }
    }

    private fun finishSelection() {
        if (resultKey.isBlank()) {
            findNavController().popBackStack()
            return
        }

        val selectedContacts =
            contacts
                .filter { selectedContactIds.contains(it.id) }
                .sortedBy { it.name }

        findNavController().previousBackStackEntry
            ?.savedStateHandle
            ?.set(
                resultKey,
                ContactSelectionResult(ArrayList(selectedContacts)),
            )

        findNavController().popBackStack()
    }

    private fun updateDoneButtonState() {
        binding.doneButton.text =
            getString(R.string.done_with_count, selectedContactIds.size)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
