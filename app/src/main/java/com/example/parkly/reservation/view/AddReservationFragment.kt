package com.example.parkly.reservation.view

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.parkly.R
import com.example.parkly.data.Interview
import com.example.parkly.data.Time
import com.example.parkly.data.viewmodel.InterviewViewModel
import com.example.parkly.data.viewmodel.JobApplicationViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentScheduleInterviewBinding
import com.example.parkly.data.viewmodel.JobViewModel
import com.example.parkly.databinding.FragmentAddReservationBinding
import com.example.parkly.reservation.viewmodel.ReservationViewModel
import com.example.parkly.util.dialog
import com.example.parkly.util.disable
import com.example.parkly.util.displayDate
import com.example.parkly.util.sendPushNotification
import com.example.parkly.util.setImageBlob
import com.example.parkly.util.snackbar
import com.example.parkly.util.toBitmap
import com.example.parkly.util.toast
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
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
    private val nav by lazy { findNavController() }

    private var date: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddReservationBinding.inflate(inflater, container, false)
        binding.topAppBar.setOnClickListener { nav.navigateUp() }

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

    private fun delete() {
        dialog(
            "Delete Interview",
            "Are you sure to delete this interview?",
            onPositiveClick = { _, _ ->
                lifecycleScope.launch {
                    interviewVM.delete(interviewID)
                }
                isDeleting = true
            })
    }

    private fun viewModeUI() {
        binding.apply {
            btnApply.disable()
            topAppBar.title = "Interview History"
            btnApply.text = "VIEW ONLY"
            chipEndTime.disable()
            chipStartTime.disable()
            chipDate.disable()
            edtLocation.disable()
            edtRemark.disable()
            edtVideo.disable()
        }
    }

    private fun fetchInterviewData() {
        interviewVM.getInterviewLD().observe(viewLifecycleOwner) {
            val interview = interviewVM.get(interviewID)
            if (isDeleting) return@observe
            if (interview == null) {
                nav.navigateUp()
                toast("Interview Data Is Empty!")
                return@observe
            }

            binding.edtLocation.setText(interview.location)
            binding.edtRemark.setText(interview.remark)
            binding.edtVideo.setText(interview.video)

            binding.chipDate.text = displayDate(interview.date)
            binding.chipStartTime.text =
                format("%02d : %02d", interview.startTime.hour, interview.startTime.minutes)
            binding.chipEndTime.text =
                format("%02d : %02d", interview.endTime.hour, interview.endTime.minutes)

            date = interview.date
            startTime = interview.startTime
            endTime = interview.endTime
        }
    }

    private fun submit() {
        val location = binding.edtLocation.text.toString().trim()
        val video = binding.edtVideo.text.toString().trim()
        val remark = binding.edtRemark.text.toString().trim()

        if (location == "" && video == "") {
            toast("Please fill in location or video conferencing link.")
            return
        }

        if (date == 0L || startTime == Time() || endTime == Time()) {
            toast("Please select date and time.")
            return
        }

        if (startTime.combinedTime > endTime.combinedTime) {
            toast("Start time must before the end time.")
            return
        }

        val interview = Interview(
            id = interviewID,
            jobAppID = jobAppID,
            location = location,
            video = video,
            remark = remark,
            startTime = startTime,
            endTime = endTime,
            date = date
        )

        interview.jobApp = jobAppVM.get(interview.jobAppID)!!
        interview.jobApp.job = jobVM.get(interview.jobApp.jobId)!!
        interview.jobApp.user = userVM.get(interview.jobApp.userId)!!

        val title = if (action == "EDIT") "Edit Interview" else "Schedule Interview"
        dialog(title, "Are you sure to ${title.lowercase()}?",
            onPositiveClick = { _, _ ->
                lifecycleScope.launch {
                    interviewVM.set(interview)
                }

                sendPushNotification(
                    if (action == "EDIT") "INTERVIEW UPDATED !" else "NEW INTERVIEW SCHEDULED !",
                    if (action == "EDIT") "Interview of ${interview.jobApp.job.jobName} has been updated to ${
                        displayDate(
                            interview.date
                        )
                    }."
                    else "Interview of ${interview.jobApp.job.jobName} has been scheduled on ${
                        displayDate(
                            interview.date
                        )
                    }.",
                    interview.jobApp.user.token
                )
            })
    }

    private fun setupDateTimePicker() {
        val constraint =
            CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build()

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraint)
                .build()

        val startTimePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Start Time")
                .build()

        val endTimePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(30)
                .setTitleText("End Time")
                .build()

        binding.chipDate.setOnClickListener {
            datePicker.show(childFragmentManager, "datePicker")
        }

        binding.chipStartTime.setOnClickListener {
            startTimePicker.show(childFragmentManager, "startTimePicker")
        }
        binding.chipEndTime.setOnClickListener {
            endTimePicker.show(childFragmentManager, "endTimePicker")
        }

        datePicker.addOnPositiveButtonClickListener {
            val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            binding.chipDate.text = format.format(Date(it))
            date = it
        }

        startTimePicker.addOnPositiveButtonClickListener {
            binding.chipStartTime.text =
                format("%02d : %02d", startTimePicker.hour, startTimePicker.minute)
            startTime = Time(startTimePicker.hour, startTimePicker.minute)
        }

        endTimePicker.addOnPositiveButtonClickListener {
            binding.chipEndTime.text =
                format("%02d : %02d", endTimePicker.hour, endTimePicker.minute)
            endTime = Time(endTimePicker.hour, endTimePicker.minute)

        }
    }




}