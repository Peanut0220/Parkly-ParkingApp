package com.example.parkly.reservation.view

import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.parkly.R
import com.example.parkly.data.Interview
import com.example.parkly.data.JobApplication
import com.example.parkly.data.Pdf
import com.example.parkly.data.Reservation
import com.example.parkly.data.Time
import com.example.parkly.data.viewmodel.InterviewViewModel
import com.example.parkly.data.viewmodel.JobApplicationViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentScheduleInterviewBinding
import com.example.parkly.data.viewmodel.JobViewModel
import com.example.parkly.databinding.FragmentAddReservationBinding
import com.example.parkly.reservation.viewmodel.ReservationViewModel
import com.example.parkly.util.JobApplicationState
import com.example.parkly.util.dialog
import com.example.parkly.util.disable
import com.example.parkly.util.displayDate
import com.example.parkly.util.sendPushNotification
import com.example.parkly.util.setImageBlob
import com.example.parkly.util.showFileSize
import com.example.parkly.util.snackbar
import com.example.parkly.util.toBitmap
import com.example.parkly.util.toast
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.SearchResultsView
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.launch
import okhttp3.internal.format
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddReservationFragment : Fragment() {

    companion object {
        fun newInstance() = AddReservationFragment()
    }

    private val userVM: UserViewModel by activityViewModels()
    private val reservationVM: ReservationViewModel by viewModels()
    private lateinit var binding: FragmentAddReservationBinding
    private var fileUri: Uri? = null
    private val nav by lazy { findNavController() }

    private var startTime: Int = 0
    private var date: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddReservationBinding.inflate(inflater, container, false)
        binding.file.visibility = View.GONE
        binding.topAppBar.setOnClickListener { nav.navigateUp() }
        binding.btnUpload.setOnClickListener { getContent.launch("application/pdf") }

        val user = userVM.get(userVM.getAuth().uid)
        if(user!=null){
            val avatar =
                if (user.avatar.toBytes().isEmpty())
                    R.drawable.round_account_circle_24
                else
                    user.avatar.toBitmap()

            binding.avatarView.loadImage(avatar)
        }

        binding.btnApply.setOnClickListener { submit() }

        setupDateTimePicker()



        return binding.root
    }




    private fun submit() {

        if (date == 0L || binding.chipStartTime.text=="Set Time"||binding.chipEndTime.text=="Set Duration") {
            toast("Please select date and time.")
            return
        }
        if(binding.edtInfo.text.toString()==""){
            toast("Please provide reason.")
            return
        }

        if (fileUri == null) {
            toast("Please Upload Your Supported File!")
            return
        }



        findNavController().navigate(
            R.id.action_addReservationFragment_to_parkingLotFragment, bundleOf(
                "action" to "reserve",
                "date" to date,
                "startTime" to startTime,
                "duration" to binding.chipEndTime.text,
                "fileUri" to fileUri.toString(),
                "fileName" to binding.fileName.text.toString(),
                "reason" to binding.edtInfo.text.toString()
            )
        )

    }

    private fun setupDateTimePicker() {
        val constraint = CalendarConstraints.Builder()
            .setValidator(TomorrowDateValidator())
            .build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setCalendarConstraints(constraint)
            .build()

        val startTimePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12) // Default hour
            .setMinute(0) // Lock minutes to 0
            .setTitleText("Start Time (Hour Only)")
            .build()

        binding.chipDate.setOnClickListener {
            datePicker.show(childFragmentManager, "datePicker")
        }

        binding.chipStartTime.setOnClickListener {
            startTimePicker.show(childFragmentManager, "startTimePicker")
        }

        binding.chipEndTime.setOnClickListener {
            val durations = arrayOf("1 Hour", "2 Hours", "3 Hours", "4 Hours")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Duration")
                .setItems(durations) { _, which ->
                    binding.chipEndTime.text = durations[which]
                }
                .show()
        }

        datePicker.addOnPositiveButtonClickListener {
            val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            binding.chipDate.text = format.format(Date(it))
            date = it
        }

        startTimePicker.addOnPositiveButtonClickListener {
            val selectedHour = startTimePicker.hour
            binding.chipStartTime.text = String.format("%02d:00", selectedHour) // Force minutes to 00
            startTime = selectedHour
        }
    }


    // Custom DateValidator to allow only dates starting tomorrow
    class TomorrowDateValidator : CalendarConstraints.DateValidator {
        override fun isValid(date: Long): Boolean {
            val today = MaterialDatePicker.todayInUtcMilliseconds()
            return date > today
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            // No additional data to write
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<TomorrowDateValidator> {
            override fun createFromParcel(parcel: Parcel): TomorrowDateValidator {
                return TomorrowDateValidator()
            }

            override fun newArray(size: Int): Array<TomorrowDateValidator?> {
                return arrayOfNulls(size)
            }
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@registerForActivityResult

            fileUri = uri
            binding.file.visibility = View.VISIBLE

            val file = uri.let { DocumentFile.fromSingleUri(requireActivity(), it) }!!
            binding.fileName.text = file.name.toString()
            binding.fileSize.text = showFileSize(file.length())

        }






}