package com.planner.library.contacts_manager.data

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ContactsPickerDataModule {
    @Provides
    @Singleton
    fun providesContentResolver(
        @ApplicationContext context: Context,
    ): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun providesDeviceContactsDataSource(
        contentResolver: ContentResolver,
    ): DeviceContactsDataSource = AndroidDeviceContactsDataSource(contentResolver)

    @Provides
    @Singleton
    fun providesContactsRepository(
        dataSource: DeviceContactsDataSource,
    ): ContactsRepository = DefaultContactsRepository(dataSource)
}
