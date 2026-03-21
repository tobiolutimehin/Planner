package com.planner.library.contacts_manager.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
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
    private var currentPrimaryAction: ContactsPickerPrimaryAction? = null
    private var isReturningFromSettings = false

    @Inject
    internal lateinit var contactsPermissionManager: ContactsPermissionManager

    private val preselectedContactIds: LongArray
        get() = arguments?.getLongArray(ContactPickerContract.PRESELECTED_CONTACT_IDS) ?: LongArray(0)

    private val resultKey: String
        get() = arguments?.getString(ContactPickerContract.RESULT_KEY).orEmpty()

    private val requestContactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.onAction(
                ContactsPickerAction.PermissionUpdated(
                    contactsPermissionManager.permissionStateFromRequestResult(
                        granted = granted,
                        shouldShowRationale = shouldShowContactsPermissionRationale(),
                    ),
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
            handlePrimaryAction()
        }

        observeUiState()
        observeEvents()

        viewModel.onAction(ContactsPickerAction.Initialize(preselectedContactIds.toSet()))
        syncPermissionState()
    }

    override fun onResume() {
        super.onResume()
        if (isReturningFromSettings) {
            isReturningFromSettings = false
            syncPermissionState()
        }
    }

    private fun syncPermissionState() {
        val permissionState =
            contactsPermissionManager.permissionState(
                context = requireContext(),
                shouldShowRationale = shouldShowContactsPermissionRationale(),
            )
        viewModel.onAction(ContactsPickerAction.PermissionUpdated(permissionState))
        if (permissionState == ContactsPermissionState.REQUEST_REQUIRED) {
            requestContactsPermission()
        }
    }

    private fun requestContactsPermission() {
        contactsPermissionManager.markPermissionRequested()
        requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun handlePrimaryAction() {
        when (currentPrimaryAction) {
            ContactsPickerPrimaryAction.REQUEST_PERMISSION -> requestContactsPermission()
            ContactsPickerPrimaryAction.OPEN_SETTINGS -> openAppSettings()
            ContactsPickerPrimaryAction.RETRY_LOAD -> viewModel.onAction(ContactsPickerAction.RetryLoad)
            null -> Unit
        }
    }

    private fun openAppSettings() {
        isReturningFromSettings = true
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireContext().packageName, null),
            ),
        )
    }

    private fun shouldShowContactsPermissionRationale(): Boolean =
        shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    currentPrimaryAction = state.primaryAction
                    adapter.submitContacts(state.contacts, state.selectedIds)

                    binding.loadingIndicator.isVisible = state.isLoading
                    binding.doneButton.isEnabled = state.isDoneEnabled
                    binding.doneButton.text = getString(R.string.done_with_count, state.selectedIds.size)

                    binding.contactsRecyclerView.isVisible =
                        !state.isLoading &&
                            state.permissionState == ContactsPermissionState.GRANTED &&
                            state.contacts.isNotEmpty()

                    val messageResId = messageResIdFor(state.status)
                    binding.emptyState.isVisible = !state.isLoading && messageResId != null
                    binding.emptyState.text = messageResId?.let(::getString).orEmpty()

                    val actionButtonLabelResId = actionButtonLabelResIdFor(state.primaryAction)
                    binding.retryButton.isVisible = !state.isLoading && actionButtonLabelResId != null
                    binding.retryButton.text = actionButtonLabelResId?.let(::getString).orEmpty()
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

    private fun messageResIdFor(status: ContactsPickerStatus): Int? =
        when (status) {
            ContactsPickerStatus.EMPTY -> R.string.no_contacts_available
            ContactsPickerStatus.SHOW_RATIONALE -> R.string.contacts_permission_rationale
            ContactsPickerStatus.OPEN_SETTINGS -> R.string.contacts_permission_open_settings
            ContactsPickerStatus.LOAD_FAILED -> R.string.contacts_load_failed
            ContactsPickerStatus.NONE -> null
        }

    private fun actionButtonLabelResIdFor(action: ContactsPickerPrimaryAction?): Int? =
        when (action) {
            ContactsPickerPrimaryAction.REQUEST_PERMISSION -> R.string.grant_contacts_permission
            ContactsPickerPrimaryAction.OPEN_SETTINGS -> R.string.open_settings
            ContactsPickerPrimaryAction.RETRY_LOAD -> R.string.retry
            null -> null
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
