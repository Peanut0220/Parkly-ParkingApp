package com.example.parkly.profile.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.data.User
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentProfileUpdateBinding
import com.example.parkly.util.cropToBlob
import com.example.parkly.util.displayDate
import com.example.parkly.util.snackbar
import com.example.parkly.util.toBitmap
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProfileUpdateFragment : Fragment() {

    lateinit var binding: FragmentProfileUpdateBinding
    val userVM: UserViewModel by viewModels()
    private val nav by lazy { findNavController() }
    private var date: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileUpdateBinding.inflate(inflater, container, false)

        binding.topAppBar.setOnClickListener { findNavController().navigateUp() }

        binding.avatar.setOnClickListener { getContent.launch("image/*") }

        binding.btnDone.setOnClickListener { submit() }

        userVM.getUserLD().observe(viewLifecycleOwner) { user ->
            binding.edtName.setText(user.name)
            binding.edtIC.setText(user.ic)
            binding.edtPhone.setText(user.phone)
            binding.chipDate.text = displayDate(user.dob)

            val avatar =
                if (user.avatar.toBytes().isEmpty())
                    R.drawable.round_account_circle_24
                else
                    user.avatar.toBitmap()

            binding.avatar.loadImage(avatar)
        }
        setupDateTimePicker()

        userVM.response.observe(viewLifecycleOwner) {
            if (it) {
                snackbar("Profile updated successfully!")
               nav.navigate(R.id.homeFragment)
            }
        }

        return binding.root
    }

    private fun submit() {
        val name = binding.edtName.text.toString().trim()
        val ic = binding.edtIC.text.toString().trim()
        val phone = binding.edtPhone.text.toString().trim()
        val avatar = binding.avatar.cropToBlob(300, 300)
        val dateText = binding.chipDate.text.toString()
        val type = "Driver"
        val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        val date = format.parse(dateText)
        val dob = date?.time ?: 0L // Convert to Long (in milliseconds) or default to 0 if null


        // Regular expression for Malaysian phone numbers
        val phoneRegex = Regex("^01[0-9]-?[0-9]{7,8}$")
        // Regular expression for Malaysian IC numbers
        val icRegex = Regex("^\\d{6}-\\d{2}-\\d{4}$")

        // Check if any required fields are empty
        if (name.isEmpty() || ic.isEmpty() || phone.isEmpty()) {
            snackbar("Please fill in all required fields.")
            return
        }

        // Validate phone number
        if (!phone.matches(phoneRegex)) {
            snackbar("Please enter a valid Malaysian phone number. (011-12342345)")
            return
        }

        // Validate IC number
        if (!ic.matches(icRegex)) {
            snackbar("Please enter a valid Malaysian IC number.(XXXXXX-XX-XXXX)")
            return
        }

        // Proceed with submission if all validations pass
        lifecycleScope.launch {
            userVM.update(User(name = name, ic = ic, phone = phone, avatar = avatar,dob = dob,type = "Driver"))
        }
    }

    private fun setupDateTimePicker() {
        // Calculate the date 18 years ago from today
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -18)
        val defaultSelectableDate = calendar.timeInMillis

        // Calculate yesterday's date to make sure today's date is excluded
        val today = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1) // Set to yesterday
        }.timeInMillis

        val constraint = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(today)) // Only dates strictly before today are selectable
            .setEnd(today) // Set the end date to yesterday
            .build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(defaultSelectableDate) // Set default date to 18 years ago
            .setCalendarConstraints(constraint)
            .build()

        binding.chipDate.setOnClickListener {
            datePicker.show(childFragmentManager, "datePicker")
        }

        datePicker.addOnPositiveButtonClickListener {
            val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            binding.chipDate.text = format.format(Date(it))
            date = it
        }
    }

    // Get-content launcher
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) return@registerForActivityResult
        binding.avatar.loadImage(it)
    }

}