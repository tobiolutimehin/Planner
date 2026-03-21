package com.planner.library.contacts_manager.data

import com.planner.library.contacts_manager.api.PickerContact
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

internal interface ContactsRepository {
    suspend fun getContacts(): List<PickerContact>
}

@Singleton
internal class DefaultContactsRepository @Inject constructor(
    private val dataSource: DeviceContactsDataSource,
) : ContactsRepository {
    private val cacheMutex = Mutex()
    private var cachedContacts: List<PickerContact>? = null

    override suspend fun getContacts(): List<PickerContact> =
        cacheMutex.withLock {
            cachedContacts ?: dataSource.fetchContacts()
                .distinctBy { it.id }
                .sortedBy { it.name.lowercase() }
                .also { cachedContacts = it }
        }
}
