package com.planner.library.contacts_manager.permission

import org.junit.Assert.assertEquals
import org.junit.Test

class ContactsPermissionManagerTest {
    private val permissionManager = ContactsPermissionManager()

    @Test
    fun permissionState_mapsGrantedAndMissingPermission() {
        assertEquals(
            ContactsPermissionState.GRANTED,
            permissionManager.permissionState(isGranted = true),
        )
        assertEquals(
            ContactsPermissionState.REQUEST_REQUIRED,
            permissionManager.permissionState(isGranted = false),
        )
    }

    @Test
    fun permissionStateFromRequestResult_mapsDeniedState() {
        assertEquals(
            ContactsPermissionState.DENIED,
            permissionManager.permissionStateFromRequestResult(granted = false),
        )
    }
}
