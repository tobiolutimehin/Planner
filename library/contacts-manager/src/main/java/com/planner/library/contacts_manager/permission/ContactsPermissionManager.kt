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
    SHOW_RATIONALE,
    OPEN_SETTINGS,
}

internal interface ContactsPermissionManager {
    fun permissionState(
        context: Context,
        shouldShowRationale: Boolean,
    ): ContactsPermissionState

    fun permissionState(
        isGranted: Boolean,
        shouldShowRationale: Boolean,
    ): ContactsPermissionState

    fun permissionStateFromRequestResult(
        granted: Boolean,
        shouldShowRationale: Boolean,
    ): ContactsPermissionState

    fun markPermissionRequested()
}

internal class DefaultContactsPermissionManager @Inject constructor(
    private val permissionRequestStore: PermissionRequestStore,
) : ContactsPermissionManager {
    override fun permissionState(
        context: Context,
        shouldShowRationale: Boolean,
    ): ContactsPermissionState =
        permissionState(
            isGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS,
                ) == PackageManager.PERMISSION_GRANTED,
            shouldShowRationale = shouldShowRationale,
        )

    override fun permissionState(
        isGranted: Boolean,
        shouldShowRationale: Boolean,
    ): ContactsPermissionState =
        when {
            isGranted -> ContactsPermissionState.GRANTED
            shouldShowRationale -> ContactsPermissionState.SHOW_RATIONALE
            permissionRequestStore.hasRequestedContactsPermission() -> ContactsPermissionState.OPEN_SETTINGS
            else -> ContactsPermissionState.REQUEST_REQUIRED
        }

    override fun permissionStateFromRequestResult(
        granted: Boolean,
        shouldShowRationale: Boolean,
    ): ContactsPermissionState =
        when {
            granted -> ContactsPermissionState.GRANTED
            shouldShowRationale -> ContactsPermissionState.SHOW_RATIONALE
            else -> ContactsPermissionState.OPEN_SETTINGS
        }

    override fun markPermissionRequested() {
        permissionRequestStore.markContactsPermissionRequested()
    }
}

internal interface PermissionRequestStore {
    fun hasRequestedContactsPermission(): Boolean

    fun markContactsPermissionRequested()
}

internal class SharedPreferencesPermissionRequestStore(
    private val context: Context,
) : PermissionRequestStore {
    override fun hasRequestedContactsPermission(): Boolean =
        preferences.getBoolean(KEY_CONTACTS_PERMISSION_REQUESTED, false)

    override fun markContactsPermissionRequested() {
        preferences.edit().putBoolean(KEY_CONTACTS_PERMISSION_REQUESTED, true).apply()
    }

    private val preferences
        get() = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private companion object {
        const val PREFERENCES_NAME = "contacts_picker_permission_store"
        const val KEY_CONTACTS_PERMISSION_REQUESTED = "contacts_permission_requested"
    }
}
