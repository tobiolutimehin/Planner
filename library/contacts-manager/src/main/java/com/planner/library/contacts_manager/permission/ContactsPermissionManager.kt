package com.planner.library.contacts_manager.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import javax.inject.Inject

internal enum class ContactsPermissionState {
    UNKNOWN,
    GRANTED,
    REQUEST_REQUIRED,
    DENIED,
}

internal class ContactsPermissionManager @Inject constructor() {
    fun permissionState(context: Context): ContactsPermissionState =
        permissionState(
            isGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS,
                ) == PackageManager.PERMISSION_GRANTED,
        )

    fun permissionState(isGranted: Boolean): ContactsPermissionState =
        if (isGranted) {
            ContactsPermissionState.GRANTED
        } else {
            ContactsPermissionState.REQUEST_REQUIRED
        }

    fun permissionStateFromRequestResult(granted: Boolean): ContactsPermissionState =
        if (granted) {
            ContactsPermissionState.GRANTED
        } else {
            ContactsPermissionState.DENIED
        }
}
