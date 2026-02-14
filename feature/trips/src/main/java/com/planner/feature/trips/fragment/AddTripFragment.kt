package com.planner.feature.trips.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.planner.core.data.entity.SavedContactEntity
import com.planner.core.data.entity.TripEntity
import com.planner.core.domain.FormatDateUseCase
import com.planner.feature.trips.R
import com.planner.feature.trips.databinding.FragmentAddTripBinding
import com.planner.feature.trips.viewmodel.TripsViewModel
import com.planner.library.contacts_manager.ContactListRecyclerAdapter
import com.planner.library.contacts_manager.ContactPickerArgs
import com.planner.library.contacts_manager.ContactSelectionResult
import com.planner.library.contacts_manager.PickerContact

class AddTripFragment : Fragment() {
    private val arguments: AddTripFragmentArgs by navArgs()
    private val tripViewModel: TripsViewModel by activityViewModels()

    private lateinit var contactsAdapter: ContactListRecyclerAdapter

    private var _binding: FragmentAddTripBinding? = null
    private val binding get() = _binding!!

    private var storageUri: String? = null
    private val formatDateUseCase = FormatDateUseCase()
    private lateinit var trip: TripEntity

    private val selectedTripMates = mutableListOf<PickerContact>()
    private val contactsResultKey = "add_trip_contacts_result"

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val bitmap = tripViewModel.getBitmapFromUri(requireContext(), uri)
                storageUri =
                    bitmap?.let { tripViewModel.saveBitmapToInternalStorage(it, requireContext()) }
                binding.tripImage.apply {
                    setImageURI(storageUri?.toUri())
                    contentDescription = context.getString(R.string.image_description)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        observeContactsSelectionResult()

        val id = arguments.tripId
        if (id > 0) {
            tripViewModel.getTripWithMates(id).observe(viewLifecycleOwner) { tripWithMates ->
                trip = tripWithMates.trip
                selectedTripMates.clear()
                selectedTripMates.addAll(
                    tripWithMates.mates.map {
                        PickerContact(id = it.contactId, name = it.name, phone = it.phone)
                    },
                )
                bind(trip)
                renderSelectedContacts()
            }
        }

        contactsAdapter = ContactListRecyclerAdapter(showSelection = false)

        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(arguments.title)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = tripViewModel
            fragment = this@AddTripFragment
            goingContacts.adapter = contactsAdapter
            tripTitleEditText.addTextChangedListener(textWatcher)
            departureDateEditText.addTextChangedListener(textWatcher)
        }

        renderSelectedContacts()
    }

    private fun observeContactsSelectionResult() {
        val navBackStackEntry = findNavController().currentBackStackEntry ?: return
        val observer =
            Observer<ContactSelectionResult> { result ->
                selectedTripMates.clear()
                selectedTripMates.addAll(result.contacts)
                renderSelectedContacts()
                navBackStackEntry.savedStateHandle.remove<ContactSelectionResult>(contactsResultKey)
            }

        navBackStackEntry.savedStateHandle
            .getLiveData<ContactSelectionResult>(contactsResultKey)
            .observe(viewLifecycleOwner, observer)
    }

    private fun renderSelectedContacts() {
        contactsAdapter.submitList(selectedTripMates.sortedBy { it.name })
    }

    private fun bind(trip: TripEntity) {
        binding.apply {
            tripTitleEditText.setText(trip.title, TextView.BufferType.SPANNABLE)
            departureDateEditText.setText(
                formatDateUseCase.format(trip.departureTime),
                TextView.BufferType.SPANNABLE,
            )
            destinationEditText.setText(trip.destination, TextView.BufferType.SPANNABLE)
            trip.tripImageUrl?.let {
                storageUri = it
                tripImage.setImageURI(it.toUri())
            }
            updateSaveButton()
            saveButton.setOnClickListener { update() }
        }
    }

    fun openContactManager() {
        findNavController().navigate(
            R.id.action_addTripFragment_to_contacts_manager_navigation_graph,
            bundleOf(
                ContactPickerArgs.PRESELECTED_CONTACT_IDS to selectedTripMates.map { it.id }.toLongArray(),
                ContactPickerArgs.RESULT_KEY to contactsResultKey,
            ),
        )
    }

    fun openDatePicker() {
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(context?.getString(R.string.select_date))
                .setSelection(
                    formatDateUseCase.getTimeLong(binding.departureDateEditText.text?.toString())
                        ?: MaterialDatePicker.todayInUtcMilliseconds(),
                )
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

        datePicker.addOnPositiveButtonClickListener {
            binding.departureDate.editText?.setText(formatDateUseCase.format(it))
        }
        datePicker.show(requireActivity().supportFragmentManager, TAG)
    }

    fun openPhotoPicker() =
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    fun close() {
        if (!findNavController().popBackStack()) activity?.finish()
    }

    fun save() {
        tripViewModel.insert(
            tripImageUrl = storageUri,
            departureTime = formatDateUseCase.getTimeLong(binding.departureDateEditText.text.toString())!!,
            destination = binding.destinationEditText.text.toString(),
            title = binding.tripTitleEditText.text.toString(),
            mates = selectedTripMates.toSavedContacts(),
        )
        goToListTripFragment()
    }

    private fun update() {
        tripViewModel.update(
            tripImageUrl = storageUri,
            departureTime = formatDateUseCase.getTimeLong(binding.departureDateEditText.text.toString())!!,
            destination = binding.destinationEditText.text.toString(),
            title = binding.tripTitleEditText.text.toString(),
            trip = trip,
            mates = selectedTripMates.toSavedContacts(),
        )
        goToListTripFragment()
    }

    private fun goToListTripFragment() {
        val action = AddTripFragmentDirections.actionAddTripFragmentToListTripFragment()
        findNavController().navigate(action)
    }

    private fun updateSaveButton() {
        binding.saveButton.isEnabled = isButtonEnabled()
    }

    fun isButtonEnabled(): Boolean {
        return with(binding) {
            !tripTitleEditText.text.isNullOrBlank() &&
                !departureDateEditText.text.isNullOrBlank() &&
                !destinationEditText.text.isNullOrBlank()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private val textWatcher =
        object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int,
            ) {}

            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int,
            ) {
                updateSaveButton()
            }
        }

    companion object {
        const val TAG = "AddTripFragment"
    }
}

private fun List<PickerContact>.toSavedContacts(): List<SavedContactEntity> =
    map {
        SavedContactEntity(
            contactId = it.id,
            name = it.name,
            phone = it.phone,
        )
    }
