package com.planner.feature.trips.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.planner.core.data.entity.Contact
import com.planner.core.domain.ContactFetcher
import com.planner.core.domain.ContactFetcherImpl
import com.planner.feature.trips.adapter.ContactListAdapter
import com.planner.feature.trips.databinding.FragmentContactListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactListFragment : Fragment() {

    private lateinit var contacts: List<Contact>
    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    private lateinit var contactFetcher: ContactFetcher
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ContactListAdapter {}

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                contactFetcher = ContactFetcherImpl(requireContext(), requireActivity())
                contacts = contactFetcher.fetchContactList().toList()
            }

            withContext(Dispatchers.Main) {
                adapter.submitList(contacts)
            }
        }
//        adapter.submitList(contacts)

        binding.apply {
            contactList.adapter = adapter
        }
    }
}
