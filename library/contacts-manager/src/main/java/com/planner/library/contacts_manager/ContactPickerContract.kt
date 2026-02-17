package com.planner.library.contacts_manager

import android.os.Parcelable
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

object ContactPickerArgs {
    const val PRESELECTED_CONTACT_IDS = "preselectedContactIds"
    const val RESULT_KEY = "resultKey"
}
