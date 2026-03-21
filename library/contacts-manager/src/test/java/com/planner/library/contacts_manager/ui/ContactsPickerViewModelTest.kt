package com.planner.library.contacts_manager.ui

import com.planner.library.contacts_manager.api.PickerContact
import com.planner.library.contacts_manager.data.ContactsRepository
import com.planner.library.contacts_manager.permission.ContactsPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsPickerViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun grantedPermission_loadsContacts() = runTest(dispatcher) {
        val viewModel =
            ContactsPickerViewModel(
                FakeContactsRepository(
                    contacts =
                        listOf(
                            PickerContact(1L, "Alex", "111"),
                            PickerContact(2L, "Beth", "222"),
                        ),
                ),
            )

        viewModel.onAction(ContactsPickerAction.Initialize(setOf(2L)))
        viewModel.onAction(ContactsPickerAction.PermissionUpdated(ContactsPermissionState.GRANTED))
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.contacts.size)
        assertEquals(setOf(2L), viewModel.uiState.value.selectedIds)
        assertEquals(ContactsPickerStatus.NONE, viewModel.uiState.value.status)
        assertTrue(viewModel.uiState.value.isDoneEnabled)
    }

    @Test
    fun missingPermission_keepsPickerInRequestRequiredStateWithoutLoading() = runTest(dispatcher) {
        val repository = FakeContactsRepository()
        val viewModel = ContactsPickerViewModel(repository)

        viewModel.onAction(
            ContactsPickerAction.PermissionUpdated(ContactsPermissionState.REQUEST_REQUIRED),
        )
        advanceUntilIdle()

        assertEquals(
            ContactsPermissionState.REQUEST_REQUIRED,
            viewModel.uiState.value.permissionState,
        )
        assertEquals(ContactsPickerStatus.NONE, viewModel.uiState.value.status)
        assertEquals(0, repository.loadCount)
    }

    @Test
    fun rationalePermission_updatesStatusAndActionWithoutLoading() = runTest(dispatcher) {
        val repository = FakeContactsRepository()
        val viewModel = ContactsPickerViewModel(repository)

        viewModel.onAction(
            ContactsPickerAction.PermissionUpdated(ContactsPermissionState.SHOW_RATIONALE),
        )
        advanceUntilIdle()

        assertEquals(
            ContactsPickerStatus.SHOW_RATIONALE,
            viewModel.uiState.value.status,
        )
        assertEquals(
            ContactsPickerPrimaryAction.REQUEST_PERMISSION,
            viewModel.uiState.value.primaryAction,
        )
        assertEquals(0, repository.loadCount)
    }

    @Test
    fun openSettingsPermission_updatesStatusAndActionWithoutLoading() = runTest(dispatcher) {
        val repository = FakeContactsRepository()
        val viewModel = ContactsPickerViewModel(repository)

        viewModel.onAction(
            ContactsPickerAction.PermissionUpdated(ContactsPermissionState.OPEN_SETTINGS),
        )
        advanceUntilIdle()

        assertEquals(
            ContactsPickerStatus.OPEN_SETTINGS,
            viewModel.uiState.value.status,
        )
        assertEquals(
            ContactsPickerPrimaryAction.OPEN_SETTINGS,
            viewModel.uiState.value.primaryAction,
        )
        assertEquals(0, repository.loadCount)
    }

    @Test
    fun toggleSelection_updatesSelectedIds() = runTest(dispatcher) {
        val viewModel = ContactsPickerViewModel(FakeContactsRepository())

        viewModel.onAction(ContactsPickerAction.Initialize(setOf(1L)))
        viewModel.onAction(ContactsPickerAction.ToggleContactSelection(2L))
        viewModel.onAction(ContactsPickerAction.ToggleContactSelection(1L))

        assertEquals(setOf(2L), viewModel.uiState.value.selectedIds)
    }

    @Test
    fun confirmSelection_emitsSortedSelectedContacts() = runTest(dispatcher) {
        val viewModel =
            ContactsPickerViewModel(
                FakeContactsRepository(
                    contacts =
                        listOf(
                            PickerContact(2L, "Zoe", "222"),
                            PickerContact(1L, "Alex", "111"),
                        ),
                ),
            )

        viewModel.onAction(ContactsPickerAction.Initialize(setOf(2L, 1L)))
        viewModel.onAction(ContactsPickerAction.PermissionUpdated(ContactsPermissionState.GRANTED))
        advanceUntilIdle()

        val eventDeferred = async { viewModel.events.first() }
        advanceUntilIdle()
        viewModel.onAction(ContactsPickerAction.ConfirmSelection)
        advanceUntilIdle()

        val event = eventDeferred.await() as ContactsPickerEvent.SelectionCompleted

        assertEquals(listOf("Alex", "Zoe"), event.result.contacts.map { it.name })
    }

    @Test
    fun repositoryFailure_setsLoadFailedState() = runTest(dispatcher) {
        val viewModel =
            ContactsPickerViewModel(
                FakeContactsRepository(throwable = IllegalStateException("boom")),
            )

        viewModel.onAction(ContactsPickerAction.PermissionUpdated(ContactsPermissionState.GRANTED))
        advanceUntilIdle()

        assertEquals(ContactsPickerStatus.LOAD_FAILED, viewModel.uiState.value.status)
        assertEquals(
            ContactsPickerPrimaryAction.RETRY_LOAD,
            viewModel.uiState.value.primaryAction,
        )
        assertFalse(viewModel.uiState.value.isDoneEnabled)
    }

    @Test
    fun emptyContacts_setsEmptyStatus() = runTest(dispatcher) {
        val viewModel = ContactsPickerViewModel(FakeContactsRepository())

        viewModel.onAction(ContactsPickerAction.PermissionUpdated(ContactsPermissionState.GRANTED))
        advanceUntilIdle()

        assertEquals(ContactsPickerStatus.EMPTY, viewModel.uiState.value.status)
        assertTrue(viewModel.uiState.value.hasLoadedContacts)
    }
}

private class FakeContactsRepository(
    private val contacts: List<PickerContact> = emptyList(),
    private val throwable: Throwable? = null,
) : ContactsRepository {
    var loadCount: Int = 0
        private set

    override suspend fun getContacts(): List<PickerContact> {
        loadCount += 1
        throwable?.let { throw it }
        return contacts
    }
}
