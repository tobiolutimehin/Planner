package com.planner.library.contacts_manager.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planner.library.contacts_manager.api.PickerContact
import com.planner.library.contacts_manager.databinding.ContactListItemBinding

internal data class ContactsPickerItemUiModel(
    val contact: PickerContact,
    val isSelected: Boolean,
)

class ContactsPickerAdapter(
    private val showSelection: Boolean = true,
    private val onSelectionChanged: ((Long, Boolean) -> Unit)? = null,
) : RecyclerView.Adapter<ContactsPickerAdapter.ViewHolder>() {
    private var items: List<ContactsPickerItemUiModel> = emptyList()

    init {
        setHasStableIds(true)
    }

    fun submitContacts(
        contacts: List<PickerContact>,
        selectedIds: Set<Long> = emptySet(),
    ) {
        items =
            contacts.map { contact ->
                ContactsPickerItemUiModel(
                    contact = contact,
                    isSelected = contact.id in selectedIds,
                )
            }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].contact.id

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
        val item = items[position]
        holder.bind(
            contact = item.contact,
            isSelected = item.isSelected,
        )
    }

    class ViewHolder(
        private val binding: ContactListItemBinding,
        private val showSelection: Boolean,
        private val onSelectionChanged: ((Long, Boolean) -> Unit)?,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            contact: PickerContact,
            isSelected: Boolean,
        ) {
            binding.contactName.text = contact.name
            binding.contactNumber.text = contact.phone

            if (!showSelection) {
                binding.button.visibility = View.GONE
                binding.root.setOnClickListener(null)
                return
            }

            binding.button.visibility = View.VISIBLE
            binding.button.setOnCheckedChangeListener(null)
            binding.button.isChecked = isSelected
            binding.button.setOnCheckedChangeListener { _, isChecked ->
                onSelectionChanged?.invoke(contact.id, isChecked)
            }

            binding.root.setOnClickListener {
                binding.button.performClick()
            }
        }
    }
}
