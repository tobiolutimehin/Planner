package com.planner.feature.trips.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.planner.core.data.entity.TripEntity
import com.planner.core.ui.BaseApplication
import com.planner.feature.trips.R
import com.planner.feature.trips.TripsViewModel
import com.planner.feature.trips.TripsViewModelFactory
import com.planner.feature.trips.databinding.FragmentAddTripBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AddTripFragment : Fragment() {

    private val tripViewModel: TripsViewModel by activityViewModels {
        TripsViewModelFactory(((activity?.application as BaseApplication).database).tripDao())
    }

    private var _binding: FragmentAddTripBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                imageUri = uri
                binding.tripImage.setImageURI(imageUri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = tripViewModel
            fragment = this@AddTripFragment
        }
    }

    fun openDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(context?.getString(R.string.select_date))
                .setSelection(
                    getTimeLong(binding.departureDateEditText.text?.toString())
                        ?: MaterialDatePicker.todayInUtcMilliseconds()
                )
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

        datePicker.addOnPositiveButtonClickListener {
            binding.departureDate.editText?.setText(outputDateFormat.format(it))
        }
        datePicker.show(requireActivity().supportFragmentManager, TAG)
    }

    fun openPhotoPicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun close() {
        if (!findNavController().popBackStack()) activity?.finish()
    }

    fun save() {
        tripViewModel.insert(
            TripEntity(
                tripImageUrl = imageUri?.toString(),
                departureTime = getTimeLong(binding.departureDateEditText.text.toString())!!,
                destination = binding.destinationEditText.text.toString(),
                title = binding.tripTitleEditText.text.toString()
            )
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    // TODO: Move this to domain layer
    private fun getTimeLong(time: String?): Long? {
        return try {
            if (time != null) {
                outputDateFormat.parse(time)?.time
            } else {
                null
            }
        } catch (_: ParseException) {
            return null
        }
    }

    // TODO: Move this to domain layer
    private val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    companion object {
        const val TAG = "AddTripFragment"
    }
}