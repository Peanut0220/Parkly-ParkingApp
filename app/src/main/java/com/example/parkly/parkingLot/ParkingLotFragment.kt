package com.example.parkly

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentParkingLotBinding
import com.example.parkly.databinding.FragmentParkingSpaceDetailsBinding
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.example.parkly.reservation.viewmodel.ReservationViewModel
import com.example.parkly.util.dialog
import com.example.parkly.util.snackbar
import com.example.parkly.util.toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class ParkingLotFragment : Fragment() {
    private lateinit var spaceVM: ParkingSpaceViewModel
    private lateinit var reservationVM: ReservationViewModel
    private lateinit var userVM: UserViewModel
    private lateinit var parkingLotView: ParkingLotView
    private val nav by lazy { findNavController() }
    private val action by lazy { arguments?.getString("action") ?: "" }
    private val date by lazy { arguments?.getLong("date") ?: 0 }
    private val startTime by lazy { arguments?.getInt("startTime") ?: 0 }
    private val duration by lazy { arguments?.getString("duration") ?: "" }
    private val fileUri by lazy { arguments?.getString("fileUri") ?: "" }
    private val reason by lazy { arguments?.getString("reason") ?: "" }
    private val fileName by lazy { arguments?.getString("fileName") ?: "" }
    private lateinit var binding: FragmentParkingLotBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParkingLotBinding.inflate(inflater, container, false)

        // Initialize ViewModels
        spaceVM = ViewModelProvider(this).get(ParkingSpaceViewModel::class.java)
        reservationVM = ViewModelProvider(this).get(ReservationViewModel::class.java)
        userVM = ViewModelProvider(this).get(UserViewModel::class.java)






        // Other setup
        if (action == "reserve") {
            binding.topAppBar.setNavigationOnClickListener {
                nav.navigateUp()
            }
        } else {
            binding.appBar.visibility = View.GONE
            binding.topAppBar.visibility = View.GONE
        }

        val activity = requireActivity() as UserActivity
        Handler(Looper.getMainLooper()).postDelayed({
            activity.setBottomNavigationVisibility(action != "reserve")
        }, 50)

        val parkingLotView = binding.parkingLotView
        parkingLotView.setViewModel(spaceVM, reservationVM, userVM)
        parkingLotView.setLifecycleOwner(viewLifecycleOwner)
        parkingLotView.setParameters(action, date, startTime, duration, fileUri, reason, fileName)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start the periodic check
        handler.post(reservationChecker)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(reservationChecker) // Clean up to avoid memory leaks
    }

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 5 * 60 * 1000L // 5 minutes in milliseconds

    private val reservationChecker = object : Runnable {
        override fun run() {
            lifecycleScope.launch {
                checkAndUpdateReservations()
            }
            handler.postDelayed(this, checkInterval) // Schedule next execution
        }
    }

    private suspend fun checkAndUpdateReservations() {
        val currentTime = System.currentTimeMillis()

        // First part: Expire old reservations
        val reservationList = reservationVM.getAll() // Assuming this returns a list of reservations

        reservationList.forEach { reservation ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = reservation.date // Date in milliseconds (e.g., Nov 29, 2024, 00:00:00)

            // Add startTime (in hours) to the calendar
            calendar.add(Calendar.HOUR_OF_DAY, reservation.startTime)
            val reservationStartTime = calendar.timeInMillis

            // Add duration (in hours) to the start time
            calendar.add(Calendar.HOUR_OF_DAY, reservation.duration)
            val reservationEndTime = calendar.timeInMillis

            // Check if the status is "Pending" or "Approved" and the reservation is expired
            if ((reservation.status == "Pending" || reservation.status == "Approved") &&
                reservationEndTime < currentTime
            ) {
                reservationVM.updateStatus(reservation.id, "Expired") // Update the reservation in the database
            }

            // If the reservation end time has passed, reset the space to "Available"
            if (reservationEndTime < currentTime) {
                val space = spaceVM.get(reservation.spaceID)
                if (space?.spaceStatus == "Reserved") {
                    spaceVM.updateStatus(reservation.spaceID, "Available") // Reset space status
                }
            }
        }

        // Second part: Update reservations within the next hour to "Reserved"
        val oneHourLater = currentTime + (60 * 60 * 1000) // 1 hour in milliseconds
        val reservations = reservationVM.getAll() // Fetch all reservations again

        reservations.forEach { reservation ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = reservation.date // Base date (e.g., Nov 29, 2024, 00:00:00)

            // Add the start time (converted to hours) to the base date
            calendar.add(Calendar.HOUR_OF_DAY, reservation.startTime)
            val reservationStartTime = calendar.timeInMillis

            // Check if the reservation is within the next hour and status is "Approved"
            if (reservationStartTime in currentTime until oneHourLater &&
                (spaceVM.get(reservation.spaceID)?.spaceStatus != "Reserved" && spaceVM.get(reservation.spaceID)?.spaceStatus!="Occupied")&&
                reservation.status == "Approved"
            ) {
                spaceVM.updateBySpaceID(reservation.spaceID) // Update the space status to "Reserved"
            }
        }
    }



}


