package com.planner.core.data.entity

/**
 * Data class for storing contact information.
 *
 * @param id The unique identifier of the contact.
 * @param name The name of the contact.
 * @param phone The phone number of the contact.
 */
data class Contact(
    val id: Int,
    val name: String,
    val phone: String,
)
