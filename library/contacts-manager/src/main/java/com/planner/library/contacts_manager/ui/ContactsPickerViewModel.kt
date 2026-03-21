package com.planner.library.contacts_manager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.library.contacts_manager.api.ContactSelectionResult
import com.planner.library.contacts_manager.data.ContactsRepository
import com.planner.library.contacts_manager.permission.ContactsPermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

internal enum class ContactsPickerStatus {
    NONE,
    EMPTY,
    SHOW_RATIONALE,
    OPEN_SETTINGS,
    LOAD_FAILED,
}

internal enum class ContactsPickerPrimaryAction {
    REQUEST_PERMISSION,
    OPEN_SETTINGS,
    RETRY_LOAD,
}

internal data class ContactsPickerUiState(
    val contacts: List<com.planner.library.contacts_manager.api.PickerContact> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val permissionState: ContactsPermissionState = ContactsPermissionState.UNKNOWN,
    val status: ContactsPickerStatus = ContactsPickerStatus.NONE,
    val primaryAction: ContactsPickerPrimaryAction? = null,
    val isDoneEnabled: Boolean = false,
    val hasLoadedContacts: Boolean = false,
)

internal sealed interface ContactsPickerAction {
    data class Initialize(
        val preselectedIds: Set<Long>,
    ) : ContactsPickerAction

    data class PermissionUpdated(
        val permissionState: ContactsPermissionState,
    ) : ContactsPickerAction

    data class ToggleContactSelection(
        val contactId: Long,
    ) : ContactsPickerAction

    data object RetryLoad : ContactsPickerAction

    data object ConfirmSelection : ContactsPickerAction
}

internal sealed interface ContactsPickerEvent {
    data class SelectionCompleted(
        val result: ContactSelectionResult,
    ) : ContactsPickerEvent
}

@HiltViewModel
internal class ContactsPickerViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactsPickerUiState())
    val uiState: StateFlow<ContactsPickerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ContactsPickerEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ContactsPickerEvent> = _events.asSharedFlow()

    private var initialized = false

    fun onAction(action: ContactsPickerAction) {
        when (action) {
            is ContactsPickerAction.Initialize -> initialize(action.preselectedIds)
            is ContactsPickerAction.PermissionUpdated -> onPermissionUpdated(action.permissionState)
            is ContactsPickerAction.ToggleContactSelection -> toggleContact(action.contactId)
            ContactsPickerAction.RetryLoad -> retryLoad()
            ContactsPickerAction.ConfirmSelection -> confirmSelection()
        }
    }

    private fun initialize(preselectedIds: Set<Long>) {
        if (initialized) {
            return
        }
        initialized = true
        _uiState.update { state ->
            state.copy(selectedIds = preselectedIds)
        }
    }

    private fun onPermissionUpdated(permissionState: ContactsPermissionState) {
        _uiState.update { state ->
            state.copy(
                permissionState = permissionState,
                status = statusForPermission(state, permissionState),
                primaryAction = primaryActionForPermission(permissionState),
                isDoneEnabled = permissionState == ContactsPermissionState.GRANTED && !state.isLoading,
            )
        }

        if (permissionState == ContactsPermissionState.GRANTED && shouldLoadContacts()) {
            loadContacts()
        }
    }

    private fun retryLoad() {
        if (_uiState.value.permissionState == ContactsPermissionState.GRANTED) {
            loadContacts()
        }
    }

    private fun shouldLoadContacts(): Boolean {
        val state = _uiState.value
        return !state.isLoading && !state.hasLoadedContacts && state.status != ContactsPickerStatus.LOAD_FAILED
    }

    private fun loadContacts() {
        if (_uiState.value.isLoading) {
            return
        }

        _uiState.update { state ->
            state.copy(
                isLoading = true,
                status = ContactsPickerStatus.NONE,
                primaryAction = null,
                isDoneEnabled = false,
            )
        }

        viewModelScope.launch {
            runCatching { contactsRepository.getContacts() }
                .onSuccess { contacts ->
                    _uiState.update { state ->
                        state.copy(
                            contacts = contacts,
                            isLoading = false,
                            status =
                                if (contacts.isEmpty()) {
                                    ContactsPickerStatus.EMPTY
                                } else {
                                    ContactsPickerStatus.NONE
                                },
                            primaryAction = null,
                            isDoneEnabled = state.permissionState == ContactsPermissionState.GRANTED,
                            hasLoadedContacts = true,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            status = ContactsPickerStatus.LOAD_FAILED,
                            primaryAction = ContactsPickerPrimaryAction.RETRY_LOAD,
                            isDoneEnabled = false,
                        )
                    }
                }
        }
    }

    private fun toggleContact(contactId: Long) {
        _uiState.update { state ->
            val updatedSelection =
                if (contactId in state.selectedIds) {
                    state.selectedIds - contactId
                } else {
                    state.selectedIds + contactId
                }

            state.copy(selectedIds = updatedSelection)
        }
    }

    private fun confirmSelection() {
        val state = _uiState.value
        val selectedContacts =
            state.contacts
                .filter { it.id in state.selectedIds }
                .sortedBy { it.name.lowercase() }

        _events.tryEmit(
            ContactsPickerEvent.SelectionCompleted(
                ContactSelectionResult(ArrayList(selectedContacts)),
            ),
        )
    }

    private fun statusForPermission(
        state: ContactsPickerUiState,
        permissionState: ContactsPermissionState,
    ): ContactsPickerStatus =
        when (permissionState) {
            ContactsPermissionState.SHOW_RATIONALE -> ContactsPickerStatus.SHOW_RATIONALE
            ContactsPermissionState.OPEN_SETTINGS -> ContactsPickerStatus.OPEN_SETTINGS
            ContactsPermissionState.GRANTED ->
                if (state.hasLoadedContacts && state.contacts.isEmpty()) {
                    ContactsPickerStatus.EMPTY
                } else {
                    ContactsPickerStatus.NONE
                }
            ContactsPermissionState.REQUEST_REQUIRED,
            ContactsPermissionState.UNKNOWN,
            -> ContactsPickerStatus.NONE
        }

    private fun primaryActionForPermission(
        permissionState: ContactsPermissionState,
    ): ContactsPickerPrimaryAction? =
        when (permissionState) {
            ContactsPermissionState.SHOW_RATIONALE -> ContactsPickerPrimaryAction.REQUEST_PERMISSION
            ContactsPermissionState.OPEN_SETTINGS -> ContactsPickerPrimaryAction.OPEN_SETTINGS
            else -> null
        }
}
