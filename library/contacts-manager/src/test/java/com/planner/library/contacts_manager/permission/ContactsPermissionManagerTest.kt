package com.planner.library.contacts_manager.permission

import org.junit.Assert.assertEquals
import org.junit.Test

class ContactsPermissionManagerTest {
    private val requestStore = FakePermissionRequestStore()
    private val permissionManager = DefaultContactsPermissionManager(requestStore)

    @Test
    fun permissionState_mapsFirstRequestState() {
        assertEquals(
            ContactsPermissionState.REQUEST_REQUIRED,
            permissionManager.permissionState(
                isGranted = false,
                shouldShowRationale = false,
            ),
        )
    }

    @Test
    fun permissionState_mapsRationaleWhenSystemRequestsIt() {
        requestStore.markContactsPermissionRequested()

        assertEquals(
            ContactsPermissionState.SHOW_RATIONALE,
            permissionManager.permissionState(
                isGranted = false,
                shouldShowRationale = true,
            ),
        )
    }

    @Test
    fun permissionState_mapsOpenSettingsAfterPriorRequestWithoutRationale() {
        requestStore.markContactsPermissionRequested()

        assertEquals(
            ContactsPermissionState.OPEN_SETTINGS,
            permissionManager.permissionState(
                isGranted = false,
                shouldShowRationale = false,
            ),
        )
    }

    @Test
    fun permissionStateFromRequestResult_mapsDeniedStateUsingRationale() {
        assertEquals(
            ContactsPermissionState.SHOW_RATIONALE,
            permissionManager.permissionStateFromRequestResult(
                granted = false,
                shouldShowRationale = true,
            ),
        )
        assertEquals(
            ContactsPermissionState.OPEN_SETTINGS,
            permissionManager.permissionStateFromRequestResult(
                granted = false,
                shouldShowRationale = false,
            ),
        )
    }
}

private class FakePermissionRequestStore : PermissionRequestStore {
    private var hasRequested = false

    override fun hasRequestedContactsPermission(): Boolean = hasRequested

    override fun markContactsPermissionRequested() {
        hasRequested = true
    }
}
