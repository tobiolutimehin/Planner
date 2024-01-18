package com.planner.library.contacts_manager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.library.contacts_manager.databinding.ContactListItemBinding

class ContactListRecyclerAdapter(private val chooseContact: (Contact) -> Unit) :
    ListAdapter<Contact, ContactListRecyclerAdapter.ViewHolder>(DiffCallback) {
    class ViewHolder(
        private var binding: ContactListItemBinding,
        val chooseContact: (Contact) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.apply {
                contactName.text = contact.name
                contactNumber.text = contact.phone
                button.setOnClickListener {
                    chooseContact(contact)
                }
            }
        }
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
            chooseContact,
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val contact = getItem(position)
        holder.bind(contact)
    }
}
