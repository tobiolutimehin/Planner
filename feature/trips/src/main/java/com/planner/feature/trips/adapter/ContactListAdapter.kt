package com.planner.feature.trips.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.core.data.entity.Contact
import com.planner.feature.trips.databinding.ContactRowBinding

class ContactListAdapter(
    private val onContactClicked: (Contact) -> Unit,
) : ListAdapter<Contact, ContactListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ContactRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            onContactClicked,
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact)
    }

    class ViewHolder(
        private var binding: ContactRowBinding,
        private val onContactClicked: (Contact) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.apply {
                contactName.text = contact.name
                contactNumber.text = contact.phone
                checkbox.setOnClickListener {
                    onContactClicked(contact)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}
