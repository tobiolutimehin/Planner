package com.planner.library.contacts_manager.api

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import kotlinx.parcelize.Parcelize

@Parcelize
data class PickerContact(
    val id: Long,
    val name: String,
    val phone: String,
) : Parcelable

@Parcelize
data class ContactSelectionResult(
    val contacts: ArrayList<PickerContact>,
) : Parcelable

object ContactPickerContract {
    const val PRESELECTED_CONTACT_IDS = "preselectedContactIds"
    const val RESULT_KEY = "resultKey"

    fun createArgs(
        preselectedContactIds: LongArray = longArrayOf(),
        resultKey: String,
    ): Bundle =
        bundleOf(
            PRESELECTED_CONTACT_IDS to preselectedContactIds,
            RESULT_KEY to resultKey,
        )

    fun consumeResult(
        savedStateHandle: SavedStateHandle,
        resultKey: String,
    ): ContactSelectionResult? {
        val result = savedStateHandle.get<ContactSelectionResult>(resultKey)
        clearResult(savedStateHandle, resultKey)
        return result
    }

    fun clearResult(
        savedStateHandle: SavedStateHandle,
        resultKey: String,
    ) {
        savedStateHandle.remove<ContactSelectionResult>(resultKey)
    }
}
