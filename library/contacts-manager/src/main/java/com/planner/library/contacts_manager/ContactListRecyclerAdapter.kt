package com.planner.library.contacts_manager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.library.contacts_manager.databinding.ContactListItemBinding

class ContactListRecyclerAdapter(
    private val showSelection: Boolean = true,
    private val onSelectionChanged: ((PickerContact, Boolean) -> Unit)? = null,
) : ListAdapter<PickerContact, ContactListRecyclerAdapter.ViewHolder>(DiffCallback) {
    private val selectedContactIds = mutableSetOf<Long>()

    init {
        setHasStableIds(true)
    }

    fun setSelectedContactIds(ids: Set<Long>) {
        selectedContactIds.clear()
        selectedContactIds.addAll(ids)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long = getItem(position).id

    class ViewHolder(
        private val binding: ContactListItemBinding,
        private val showSelection: Boolean,
        private val isSelected: (Long) -> Boolean,
        private val onSelectionChanged: ((PickerContact, Boolean) -> Unit)?,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: PickerContact) {
            binding.contactName.text = contact.name
            binding.contactNumber.text = contact.phone

            if (!showSelection) {
                binding.button.visibility = View.GONE
                binding.root.setOnClickListener(null)
                return
            }

            binding.button.visibility = View.VISIBLE
            binding.button.setOnCheckedChangeListener(null)
            binding.button.isChecked = isSelected(contact.id)
            binding.button.setOnCheckedChangeListener { _, isChecked ->
                onSelectionChanged?.invoke(contact, isChecked)
            }

            binding.root.setOnClickListener {
                binding.button.performClick()
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PickerContact>() {
        override fun areItemsTheSame(oldItem: PickerContact, newItem: PickerContact): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PickerContact, newItem: PickerContact): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ContactListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            showSelection,
            isSelected = { selectedContactIds.contains(it) },
            onSelectionChanged = onSelectionChanged,
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
