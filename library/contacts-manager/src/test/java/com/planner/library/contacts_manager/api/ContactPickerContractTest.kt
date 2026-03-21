package com.planner.library.contacts_manager.api

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContactPickerContractTest {
    @Test
    fun consumeResult_returnsAndClearsSelection() {
        val savedStateHandle = SavedStateHandle()
        val result = ContactSelectionResult(arrayListOf(PickerContact(1L, "Alex", "123")))
        savedStateHandle["result-key"] = result

        val consumed = ContactPickerContract.consumeResult(savedStateHandle, "result-key")

        assertEquals(result, consumed)
        assertNull(savedStateHandle.get<ContactSelectionResult>("result-key"))
    }

    @Test
    fun clearResult_removesSelectionWithoutConsuming() {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["result-key"] = ContactSelectionResult(arrayListOf(PickerContact(1L, "Alex", "123")))

        ContactPickerContract.clearResult(savedStateHandle, "result-key")

        assertNull(savedStateHandle.get<ContactSelectionResult>("result-key"))
    }
}
