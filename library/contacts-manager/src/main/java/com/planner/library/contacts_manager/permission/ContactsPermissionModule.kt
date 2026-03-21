package com.planner.library.contacts_manager.permission

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ContactsPermissionModule {
    @Binds
    @Singleton
    abstract fun bindContactsPermissionManager(
        manager: DefaultContactsPermissionManager,
    ): ContactsPermissionManager

    companion object {
        @Provides
        @Singleton
        fun providePermissionRequestStore(
            @ApplicationContext context: Context,
        ): PermissionRequestStore = SharedPreferencesPermissionRequestStore(context)
    }
}
