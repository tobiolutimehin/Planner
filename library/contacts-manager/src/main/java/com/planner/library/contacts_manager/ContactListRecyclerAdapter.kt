package com.planner.library.contacts_manager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.library.contacts_manager.databinding.ContactListItemBinding

// TODO: Checked button bug. It's showing regardless
class ContactListRecyclerAdapter(
    private val selectedContacts: MutableSet<Contact>,
    private val onContactSelected: (Contact, Boolean) -> Unit
) :
    ListAdapter<Contact, ContactListRecyclerAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(
        private var binding: ContactListItemBinding,
        private val onContactSelected: (Contact, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact, selectedContacts: MutableSet<Contact>) {
            binding.apply {
                contactName.text = contact.name
                contactNumber.text = contact.phone
                button.isChecked = selectedContacts.contains(contact)

                button.setOnClickListener {
                    val isSelected = button.isChecked
                    onContactSelected(contact, isSelected)
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(
            oldItem: Contact,
            newItem: Contact,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Contact,
            newItem: Contact,
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            ContactListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            onContactSelected
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val contact = getItem(position)
        holder.bind(contact, selectedContacts = selectedContacts)
    }
}
