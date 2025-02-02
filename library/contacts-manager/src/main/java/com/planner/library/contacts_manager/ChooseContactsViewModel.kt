package com.planner.library.contacts_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChooseContactsViewModel @Inject constructor(
    private val contactFetcher: ContactFetcher,
) : ViewModel() {

    // LiveData to hold the list of selected contacts
    private val _selectedContacts = MutableLiveData<MutableSet<Contact>>(mutableSetOf())
    val selectedContacts: LiveData<MutableSet<Contact>> = _selectedContacts

    private val _allContacts = MutableLiveData<List<Contact>>(emptyList())
    val allContacts: LiveData<List<Contact>> = _allContacts

    // Add or remove contacts from the selection
    fun toggleContactSelection(contact: Contact, isSelected: Boolean) {
        _selectedContacts.value?.apply {
            if (isSelected) {
                add(contact)
            } else {
                remove(contact)
            }
        }
    }

    fun setSelectedContacts(contacts: List<Contact>) {
        _selectedContacts.value = contacts.toMutableSet()
    }

    // Reset the selected contacts (e.g., when coming back from the fragment)
    fun resetSelection() {
        _selectedContacts.value = mutableSetOf()
    }

    // Fetch all contacts
    fun setAllContacts() {
        viewModelScope.launch {
            _allContacts.value = contactFetcher.fetchContactList().toList()
        }
    }

    init {
        setAllContacts()
    }
}
