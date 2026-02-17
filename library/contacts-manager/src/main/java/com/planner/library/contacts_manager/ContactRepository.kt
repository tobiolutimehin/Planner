package com.planner.library.contacts_manager

import android.provider.ContactsContract
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The interface for fetching contacts.
 */
interface ContactFetcher {
    /**
     * Fetches the contact list.
     *
     * @return the contact list.
     */
    suspend fun fetchContactList(): Set<PickerContact>
}

/**
 * The implementation of the [ContactFetcher].
 */
@Singleton
class ContactFetcherImpl @Inject constructor(
    private val contactContentResolverProvider: ContactContentResolverProvider,
) : ContactFetcher {
    private val cacheMutex = Mutex()
    private val cache: MutableSet<PickerContact> = mutableSetOf()

    override suspend fun fetchContactList(): Set<PickerContact> =
        cacheMutex.withLock {
            if (cache.isNotEmpty()) {
                return cache
            }

            val contacts =
                withContext(Dispatchers.IO) {
                    val contactList = mutableListOf<PickerContact>()
                    try {
                        val contentResolver = contactContentResolverProvider.contentResolver()
                        val cursor =
                            contentResolver.query(
                                ContactsContract.Contacts.CONTENT_URI,
                                null,
                                null,
                                null,
                                "${ContactsContract.Contacts.DISPLAY_NAME} ASC",
                            )

                        cursor?.use { csr ->
                            while (csr.moveToNext()) {
                                val id = csr.getLong(csr.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                                val name =
                                    csr.getString(
                                        csr.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME),
                                    )
                                val hasPhoneNumber =
                                    csr.getInt(
                                        csr.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER),
                                    )

                                if (hasPhoneNumber <= 0) continue

                                val phoneCursor =
                                    contentResolver.query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                        arrayOf(id.toString()),
                                        null,
                                    )

                                phoneCursor?.use { phoneCsr ->
                                    if (phoneCsr.moveToFirst()) {
                                        val number =
                                            phoneCsr.getString(
                                                phoneCsr.getColumnIndexOrThrow(
                                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                                ),
                                            )
                                        contactList.add(PickerContact(id = id, name = name, phone = number))
                                    }
                                }
                            }
                        }
                    } catch (exception: Exception) {
                        Log.e(TAG, "exception while fetching contacts", exception)
                    }
                    contactList.distinctBy { it.id }.toSet()
                }

            cache.addAll(contacts)
            cache
        }

    fun clearCache() {
        cache.clear()
    }

    companion object {
        private const val TAG = "ContactFetcherImpl"
    }
}
