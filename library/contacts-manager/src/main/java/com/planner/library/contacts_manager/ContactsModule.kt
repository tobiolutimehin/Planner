package com.planner.library.contacts_manager

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

class ContactContentResolverProvider(
    private val context: Context,
) {
    fun contentResolver(): ContentResolver = context.contentResolver
}

@Module
@InstallIn(SingletonComponent::class)
object ContactsModule {
    @Provides
    @Singleton
    fun providesContactContentResolverProvider(
        @ApplicationContext context: Context,
    ): ContactContentResolverProvider = ContactContentResolverProvider(context)

    @Provides
    @Singleton
    fun providesContactFetcher(
        provider: ContactContentResolverProvider,
    ): ContactFetcher = ContactFetcherImpl(provider)
}
