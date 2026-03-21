package com.planner.library.contacts_manager.testing

import android.content.Context
import com.planner.library.contacts_manager.api.PickerContact
import com.planner.library.contacts_manager.data.ContactsPickerDataModule
import com.planner.library.contacts_manager.data.ContactsRepository
import com.planner.library.contacts_manager.permission.ContactsPermissionManager
import com.planner.library.contacts_manager.permission.ContactsPermissionModule
import com.planner.library.contacts_manager.permission.ContactsPermissionState
import dagger.Module
import dagger.Provides
import dagger.hilt.testing.TestInstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ContactsPickerDataModule::class, ContactsPermissionModule::class],
)
internal object FakeContactsPickerTestModule {
    @Provides
    @Singleton
    fun provideContactsRepository(): ContactsRepository = FakeContactsRepository

    @Provides
    @Singleton
    fun provideContactsPermissionManager(): ContactsPermissionManager = FakeContactsPermissionManager
}

internal object FakeContactsRepository : ContactsRepository {
    var contacts: List<PickerContact> = emptyList()
    var throwable: Throwable? = null

    override suspend fun getContacts(): List<PickerContact> {
        throwable?.let { throw it }
        return contacts
    }

    fun reset() {
        contacts = emptyList()
        throwable = null
    }
}

internal object FakeContactsPermissionManager : ContactsPermissionManager {
    var currentState: ContactsPermissionState = ContactsPermissionState.GRANTED
    var requestResultState: ContactsPermissionState = ContactsPermissionState.GRANTED
    var requestCount: Int = 0

    override fun permissionState(
        context: Context,
        shouldShowRationale: Boolean,
    ): ContactsPermissionState = currentState

    override fun permissionState(
        isGranted: Boolean,
        shouldShowRationale: Boolean,
    ): ContactsPermissionState = currentState

    override fun permissionStateFromRequestResult(
        granted: Boolean,
        shouldShowRationale: Boolean,
    ): ContactsPermissionState =
        if (granted) {
            ContactsPermissionState.GRANTED
        } else {
            requestResultState
        }

    override fun markPermissionRequested() {
        requestCount += 1
    }

    fun reset() {
        currentState = ContactsPermissionState.GRANTED
        requestResultState = ContactsPermissionState.GRANTED
        requestCount = 0
    }
}
