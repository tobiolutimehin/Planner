package com.planner.library.contacts_manager.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.library.contacts_manager.api.PickerContact
import com.planner.library.contacts_manager.databinding.ContactListItemBinding

data class ContactsPickerItemUiModel(
    val contact: PickerContact,
    val isSelected: Boolean,
)

class ContactsPickerAdapter(
    private val showSelection: Boolean = true,
    private val onSelectionChanged: ((Long, Boolean) -> Unit)? = null,
) : ListAdapter<ContactsPickerItemUiModel, ContactsPickerAdapter.ViewHolder>(DiffCallback) {
    init {
        setHasStableIds(true)
    }

    fun submitContacts(
        contacts: List<PickerContact>,
        selectedIds: Set<Long> = emptySet(),
    ) {
        submitList(
            contacts.map { contact ->
                ContactsPickerItemUiModel(
                    contact = contact,
                    isSelected = contact.id in selectedIds,
                )
            },
        )
    }

    override fun getItemId(position: Int): Long = getItem(position).contact.id

    class ViewHolder(
        private val binding: ContactListItemBinding,
        private val showSelection: Boolean,
        private val onSelectionChanged: ((Long, Boolean) -> Unit)?,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContactsPickerItemUiModel) {
            binding.contactName.text = item.contact.name
            binding.contactNumber.text = item.contact.phone

            if (!showSelection) {
                binding.button.visibility = View.GONE
                binding.root.setOnClickListener(null)
                return
            }

            binding.button.visibility = View.VISIBLE
            binding.button.setOnCheckedChangeListener(null)
            binding.button.isChecked = item.isSelected
            binding.button.setOnCheckedChangeListener { _, isChecked ->
                onSelectionChanged?.invoke(item.contact.id, isChecked)
            }

            binding.root.setOnClickListener {
                binding.button.performClick()
            }
        }
    }

    private companion object DiffCallback : DiffUtil.ItemCallback<ContactsPickerItemUiModel>() {
        override fun areItemsTheSame(
            oldItem: ContactsPickerItemUiModel,
            newItem: ContactsPickerItemUiModel,
        ): Boolean = oldItem.contact.id == newItem.contact.id

        override fun areContentsTheSame(
            oldItem: ContactsPickerItemUiModel,
            newItem: ContactsPickerItemUiModel,
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ContactListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            showSelection,
            onSelectionChanged,
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
