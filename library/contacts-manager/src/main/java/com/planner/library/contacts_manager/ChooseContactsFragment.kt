package com.planner.library.contacts_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.planner.library.contacts_manager.databinding.FragmentChooseContactsBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChooseContactsFragment : Fragment() {
    private lateinit var _binding: FragmentChooseContactsBinding
    private val binding get() = _binding

    private lateinit var adapter: ContactListRecyclerAdapter

    @Inject lateinit var contactFetcher: ContactFetcher

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChooseContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ContactListRecyclerAdapter {}
        binding.chooseContactsRecyclerView.adapter = adapter

        lifecycleScope.launch {
            adapter.submitList(
                contactFetcher.fetchContactList().toList()
            )
        }
    }
}
