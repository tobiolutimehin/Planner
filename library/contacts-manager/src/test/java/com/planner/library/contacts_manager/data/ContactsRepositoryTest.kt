package com.planner.library.contacts_manager.data

import com.planner.library.contacts_manager.api.PickerContact
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactsRepositoryTest {
    @Test
    fun getContacts_deduplicatesSortsAndCaches() = runTest {
        val dataSource =
            FakeDeviceContactsDataSource(
                listOf(
                    PickerContact(2L, "Zoe", "222"),
                    PickerContact(1L, "Alex", "111"),
                    PickerContact(1L, "Alex Duplicate", "111"),
                ),
            )
        val repository = DefaultContactsRepository(dataSource)

        val firstLoad = repository.getContacts()
        val secondLoad = repository.getContacts()

        assertEquals(
            listOf(
                PickerContact(1L, "Alex", "111"),
                PickerContact(2L, "Zoe", "222"),
            ),
            firstLoad,
        )
        assertEquals(firstLoad, secondLoad)
        assertEquals(1, dataSource.fetchCount)
    }
}

private class FakeDeviceContactsDataSource(
    private val contacts: List<PickerContact>,
) : DeviceContactsDataSource {
    var fetchCount: Int = 0
        private set

    override suspend fun fetchContacts(): List<PickerContact> {
        fetchCount += 1
        return contacts
    }
}
