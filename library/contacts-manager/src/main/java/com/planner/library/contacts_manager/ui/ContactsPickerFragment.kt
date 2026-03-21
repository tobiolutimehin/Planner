package com.planner.library.contacts_manager.ui

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.planner.library.contacts_manager.R
import com.planner.library.contacts_manager.api.ContactPickerContract
import com.planner.library.contacts_manager.databinding.FragmentContactsPickerBinding
import com.planner.library.contacts_manager.permission.ContactsPermissionManager
import com.planner.library.contacts_manager.permission.ContactsPermissionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ContactsPickerFragment : Fragment() {
    private var _binding: FragmentContactsPickerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsPickerViewModel by viewModels()

    private lateinit var adapter: ContactsPickerAdapter

    @Inject
    internal lateinit var contactsPermissionManager: ContactsPermissionManager

    private val preselectedContactIds: LongArray
        get() = arguments?.getLongArray(ContactPickerContract.PRESELECTED_CONTACT_IDS) ?: LongArray(0)

    private val resultKey: String
        get() = arguments?.getString(ContactPickerContract.RESULT_KEY).orEmpty()

    private val requestContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.onAction(
                ContactsPickerAction.PermissionUpdated(
                    contactsPermissionManager.permissionStateFromRequestResult(granted),
                ),
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentContactsPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ContactsPickerAdapter { contactId, _ ->
            viewModel.onAction(ContactsPickerAction.ToggleContactSelection(contactId))
        }
        binding.contactsRecyclerView.adapter = adapter
        binding.doneButton.setOnClickListener {
            viewModel.onAction(ContactsPickerAction.ConfirmSelection)
        }
        binding.retryButton.setOnClickListener {
            requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
        }

        observeUiState()
        observeEvents()

        viewModel.onAction(ContactsPickerAction.Initialize(preselectedContactIds.toSet()))
        syncPermissionState()
    }

    private fun syncPermissionState() {
        val permissionState = contactsPermissionManager.permissionState(requireContext())
        viewModel.onAction(ContactsPickerAction.PermissionUpdated(permissionState))
        if (permissionState == ContactsPermissionState.REQUEST_REQUIRED) {
            requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitContacts(state.contacts, state.selectedIds)
                    binding.loadingIndicator.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    binding.doneButton.isEnabled = state.isDoneEnabled
                    binding.doneButton.text =
                        getString(R.string.done_with_count, state.selectedIds.size)

                    val shouldShowMessage = !state.isLoading && (
                        state.errorMessage != null ||
                            state.permissionState == ContactsPermissionState.DENIED ||
                            state.contacts.isEmpty()
                        )
                    binding.emptyState.visibility = if (shouldShowMessage) View.VISIBLE else View.GONE
                    binding.emptyState.text =
                        state.errorMessage ?: getString(R.string.no_contacts_available)
                    binding.retryButton.visibility =
                        if (state.permissionState == ContactsPermissionState.DENIED) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is ContactsPickerEvent.SelectionCompleted -> finishSelection(event.result)
                    }
                }
            }
        }
    }

    private fun finishSelection(result: com.planner.library.contacts_manager.api.ContactSelectionResult) {
        if (resultKey.isNotBlank()) {
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set(resultKey, result)
        }
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
