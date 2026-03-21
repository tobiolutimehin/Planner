package com.planner.library.contacts_manager.data

import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import com.planner.library.contacts_manager.api.PickerContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal interface DeviceContactsDataSource {
    suspend fun fetchContacts(): List<PickerContact>
}

internal class AndroidDeviceContactsDataSource @Inject constructor(
    private val contentResolver: ContentResolver,
) : DeviceContactsDataSource {
    override suspend fun fetchContacts(): List<PickerContact> =
        withContext(Dispatchers.IO) {
            val contacts = mutableListOf<PickerContact>()

            try {
                contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    "${ContactsContract.Contacts.DISPLAY_NAME} ASC",
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        val name =
                            cursor.getString(
                                cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME),
                            )
                        val hasPhoneNumber =
                            cursor.getInt(
                                cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER),
                            )

                        if (hasPhoneNumber <= 0) {
                            continue
                        }

                        contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(id.toString()),
                            null,
                        )?.use { phoneCursor ->
                            if (phoneCursor.moveToFirst()) {
                                val number =
                                    phoneCursor.getString(
                                        phoneCursor.getColumnIndexOrThrow(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                                        ),
                                    )
                                contacts.add(
                                    PickerContact(
                                        id = id,
                                        name = name,
                                        phone = number,
                                    ),
                                )
                            }
                        }
                    }
                }
            } catch (exception: Exception) {
                Log.e(TAG, "exception while fetching contacts", exception)
            }

            contacts
        }

    private companion object {
        const val TAG = "AndroidDeviceContactsDataSource"
    }
}
