package com.planner.core.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.core.data.entity.Contact
import com.planner.core.ui.databinding.ContactListItemBinding

class ContactListRecyclerAdapter(val removeContact: (Contact) -> Unit) :
    ListAdapter<Contact, ContactListRecyclerAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(
        private var binding: ContactListItemBinding,
        val removeContact: (Contact) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.apply {
                contactName.text = contact.name
                button.setOnClickListener {
                    removeContact(contact)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ContactListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            removeContact,
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact)
    }
}
