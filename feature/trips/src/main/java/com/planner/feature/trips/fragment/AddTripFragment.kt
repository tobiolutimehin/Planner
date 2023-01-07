package com.planner.feature.trips.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.planner.core.domain.FormatDateUseCase
import com.planner.core.ui.BaseApplication
import com.planner.feature.trips.R
import com.planner.feature.trips.databinding.FragmentAddTripBinding
import com.planner.feature.trips.viewmodel.TripsViewModel
import com.planner.feature.trips.viewmodel.TripsViewModelFactory

class AddTripFragment : Fragment() {

    private val tripViewModel: TripsViewModel by activityViewModels {
        TripsViewModelFactory(((activity?.application as BaseApplication).database).tripDao())
    }

    private var _binding: FragmentAddTripBinding? = null
    private val binding get() = _binding!!
    private var storageUri: String? = null
    private val formatDateUseCase = FormatDateUseCase()

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
            tripTitleEditText.addTextChangedListener(textWatcher)
            departureDateEditText.addTextChangedListener(textWatcher)
        }
    }

    fun openDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(context?.getString(R.string.select_date))
                .setSelection(
                    formatDateUseCase.getTimeLong(binding.departureDateEditText.text?.toString())
                        ?: MaterialDatePicker.todayInUtcMilliseconds()
                )
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

        datePicker.addOnPositiveButtonClickListener {
            binding.departureDate.editText?.setText(formatDateUseCase.format(it))
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
            tripImageUrl = storageUri,
            departureTime = formatDateUseCase.getTimeLong(binding.departureDateEditText.text.toString())!!,
            destination = binding.destinationEditText.text.toString(),
            title = binding.tripTitleEditText.text.toString()
        )
        val action = AddTripFragmentDirections.actionAddTripFragmentToListTripFragment()
        findNavController().navigate(action)
    }

    private fun updateSaveButton() {
        binding.saveButton.isEnabled = isButtonEnabled()
    }

    fun isButtonEnabled(): Boolean {
        return with(binding) {
            !tripTitleEditText.text.isNullOrBlank() && !departureDateEditText.text.isNullOrBlank() && !destinationEditText.text.isNullOrBlank()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

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
