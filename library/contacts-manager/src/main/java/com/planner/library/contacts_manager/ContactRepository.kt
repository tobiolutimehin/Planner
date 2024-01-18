package com.planner.library.contacts_manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

data class Contact(
    val id: Int,
    val name: String,
    val phone: String,
)

/**
 * The interface for fetching contacts.
 */
interface ContactFetcher {
    /**
     * Fetches the contact list.
     *
     * @return the contact list.
     */
    suspend fun fetchContactList(): Set<Contact>
}

/**
 * The implementation of the [ContactFetcher].
 */
class ContactFetcherImpl
    @Inject
    constructor(
        private val context: Context,
    ) : ContactFetcher {
        override suspend fun fetchContactList(): Set<Contact> {
            val contactList = mutableListOf<Contact>()
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                context.activity()?.let {
                    ActivityCompat.requestPermissions(
                        it,
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        0,
                    )
                }
            } else {
                try {
                    val contentResolver = context.contentResolver
                    val uri = ContactsContract.Contacts.CONTENT_URI
                    val cursor =
                        contentResolver.query(
                            uri,
                            null,
                            null,
                            null,
                            ContactsContract.Contacts.DISPLAY_NAME + " ASC",
                        )

                    cursor?.let { csr ->
                        if (csr.count > 0) {
                            while (csr.moveToNext()) {
                                val id =
                                    csr.getLong(csr.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                                val name =
                                    csr.getString(csr.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                                val hasPhoneNumber =
                                    csr.getInt(csr.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                                if (hasPhoneNumber > 0) {
                                    val phoneCursor =
                                        contentResolver.query(
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                            arrayOf(id.toString()),
                                            null,
                                        )

                                    phoneCursor?.let { phoneCsr ->
                                        if (phoneCsr.count > 0) {
                                            while (phoneCsr.moveToNext()) {
                                                val number =
                                                    phoneCsr.getString(
                                                        phoneCsr.getColumnIndexOrThrow(
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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesContactFetcher(
        @ApplicationContext context: Context,
    ): ContactFetcherImpl = ContactFetcherImpl(context)
}

tailrec fun Context?.activity(): Activity? =
    this as? Activity
        ?: (this as? ContextWrapper)?.baseContext?.activity()
