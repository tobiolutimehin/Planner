package com.planner.feature.trips.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.planner.library.contacts_manager.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
) : ViewModel() {
    val contacts = MutableLiveData<List<Contact>>()

    fun setContacts(selectedContacts: List<Contact>) {
        contacts.value = selectedContacts
    }
}
