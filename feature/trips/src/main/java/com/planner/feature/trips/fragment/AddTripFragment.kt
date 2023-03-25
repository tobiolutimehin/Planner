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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.planner.core.data.entity.TripEntity
import com.planner.core.domain.FormatDateUseCase
import com.planner.core.ui.BaseApplication
import com.planner.feature.trips.R
import com.planner.feature.trips.databinding.FragmentAddTripBinding
import com.planner.feature.trips.viewmodel.TripsViewModel
import com.planner.feature.trips.viewmodel.TripsViewModelFactory

class AddTripFragment : Fragment() {
    private val arguments: AddTripFragmentArgs by navArgs()

    private val tripViewModel: TripsViewModel by activityViewModels {
        TripsViewModelFactory(((activity?.application as BaseApplication).database).tripDao())
    }

    private var _binding: FragmentAddTripBinding? = null
    private val binding get() = _binding!!
    private var storageUri: String? = null
    private val formatDateUseCase = FormatDateUseCase()
    private lateinit var trip: TripEntity

    /**
     * This value registers an activity result launcher to pick an image from the device's gallery.
     * It sets the selected image to the tripImage view in the binding, and saves the image to
     * internal storage using the tripViewModel. The URI of the saved image is stored in the
     * storageUri variable.
     */
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments.tripId
        if (id > 0) {
            tripViewModel.getTrip(id).observe(this.viewLifecycleOwner) { tripModel ->
                trip = tripModel
                bind(trip)
            }
        }
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(arguments.title)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = tripViewModel
            fragment = this@AddTripFragment
            tripTitleEditText.addTextChangedListener(textWatcher)
            departureDateEditText.addTextChangedListener(textWatcher)
        }
    }

    /**
     * Binds [trip] data to the views in this fragment.
     */
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

    /**
     * Opens a date picker dialog to select a departure date for the trip.
     */
    fun openDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
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

    /**
     * Opens a photo picker dialog to select an image for the trip.
     */
    fun openPhotoPicker() =
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    /**
     * Closes the fragment, either by popping it off the navigation stack or finishing the activity if it's the only one.
     */
    fun close() {
        if (!findNavController().popBackStack()) activity?.finish()
    }

    /**
     * Saves the trip data to the database and navigates to the list of trips fragment.
     */
    fun save() {
        tripViewModel.insert(
            tripImageUrl = storageUri,
            departureTime = formatDateUseCase.getTimeLong(binding.departureDateEditText.text.toString())!!,
            destination = binding.destinationEditText.text.toString(),
            title = binding.tripTitleEditText.text.toString(),
        )
        goToListTripFragment()
    }

    /**
     * Updates the trip data in the database and navigates to the list of trips fragment.
     */
    private fun update() {
        tripViewModel.update(
            tripImageUrl = storageUri,
            departureTime = formatDateUseCase.getTimeLong(binding.departureDateEditText.text.toString())!!,
            destination = binding.destinationEditText.text.toString(),
            title = binding.tripTitleEditText.text.toString(),
            trip = trip,
        )
        goToListTripFragment()
    }

    /**
     * Navigates to the list of trips fragment.
     */
    private fun goToListTripFragment() {
        val action = AddTripFragmentDirections.actionAddTripFragmentToListTripFragment()
        findNavController().navigate(action)
    }

    /**
     * Updates the state of the save button based on whether all required fields are filled in.
     * The button is enabled if all fields are filled in, and disabled otherwise.
     */
    private fun updateSaveButton() {
        binding.saveButton.isEnabled = isButtonEnabled()
    }

    /**
     * Returns true if all required fields are filled in.
     */
    fun isButtonEnabled(): Boolean {
        return with(binding) {
            !tripTitleEditText.text.isNullOrBlank() && !departureDateEditText.text.isNullOrBlank() && !destinationEditText.text.isNullOrBlank()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * A [TextWatcher] that listens for changes in the text of the input fields in the UI,
     * and updates the state of the save button accordingly.
     */
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateSaveButton()
        }
    }

    companion object {
        const val TAG = "AddTripFragment"
    }
}
