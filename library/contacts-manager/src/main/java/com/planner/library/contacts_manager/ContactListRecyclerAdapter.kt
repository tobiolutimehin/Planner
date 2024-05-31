package com.planner.library.contacts_manager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.library.contacts_manager.databinding.ContactListItemBinding

val mutableSet: MutableSet<Long> = mutableSetOf()

class ContactListRecyclerAdapter :
    ListAdapter<Contact, ContactListRecyclerAdapter.ViewHolder>(DiffCallback) {
    class ViewHolder(
        private var binding: ContactListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.apply {
                contactName.text = contact.name
                contactNumber.text = contact.phone
                button.isChecked = mutableSet.contains(contact.id)
                button.setOnClickListener {
                    if (button.isChecked) {
                        mutableSet.add(contact.id)
                    } else {
                        mutableSet.remove(contact.id)
                    }
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
