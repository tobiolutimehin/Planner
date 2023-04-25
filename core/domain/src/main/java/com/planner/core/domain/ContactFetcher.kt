package com.planner.core.domain

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.planner.core.data.entity.Contact

interface ContactFetcher {
    fun fetchContactList(): Set<Contact>
}

class ContactFetcherImpl(
    private val context: Context,
    private val activity: Activity,
) : ContactFetcher {
    override fun fetchContactList(): Set<Contact> {
        val contactList = mutableListOf<Contact>()
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_CONTACTS),
                0,
            )
        } else {
            try {
                val contentResolver = context.contentResolver
                val uri = ContactsContract.Contacts.CONTENT_URI
                val cursor = contentResolver.query(
                    uri,
                    null,
                    null,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC",
                )

                cursor?.let { cursor ->
                    if (cursor.count > 0) {
                        while (cursor.moveToNext()) {
                            val id =
                                cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                            val name =
                                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                            val hasPhoneNumber =
                                cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                            if (hasPhoneNumber > 0) {
                                val phoneCursor = contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    arrayOf(id.toString()),
                                    null,
                                )

                                phoneCursor?.let { phoneCursor ->
                                    if (phoneCursor.count > 0) {
                                        while (phoneCursor.moveToNext()) {
                                            val number =
                                                phoneCursor.getString(
                                                    phoneCursor.getColumnIndexOrThrow(
                                                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                                                    ),
                                                )
                                            contactList.add(Contact(id.toInt(), name, number))
                                        }
                                    }
                                }
                                phoneCursor?.close()
                            }
                        }
                    }
                }
                cursor?.close()
            } catch (illegalArgumentException: IllegalArgumentException) {
                Log.e(TAG, "illegalArgumentException: $illegalArgumentException")
            } catch (exception: Exception) {
                Log.e(TAG, "exception: $exception")
            }
        }
        return contactList.distinctBy { it.id }.toSet()
    }

    companion object {
        private const val TAG = "ContactFetcherImpl"
    }
}
