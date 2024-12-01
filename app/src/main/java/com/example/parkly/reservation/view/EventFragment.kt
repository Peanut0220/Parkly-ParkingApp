package com.example.parkly.reservation.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.data.Reservation
import com.example.parkly.databinding.FragmentEventBinding
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.example.parkly.reseration.view.TabPendingReservationFragment
import com.example.parkly.reservation.viewmodel.ReservationViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class EventFragment : Fragment() {

    companion object {
        fun newInstance() = EventFragment()
    }

    private lateinit var binding: FragmentEventBinding
    private val reservationVM: ReservationViewModel by viewModels()
    private val spaceVM: ParkingSpaceViewModel by viewModels()

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 5 * 60 * 1000L // 5 minutes in milliseconds

    private val reservationChecker = object : Runnable {
        override fun run() {
            reservationVM.getreservationLD().observe(viewLifecycleOwner) { reservations ->
                lifecycleScope.launch {
                    checkAndUpdateReservations(reservations)
                }
            }
            handler.postDelayed(this, checkInterval) // Schedule next execution
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event, container, false)

        reservationVM.getreservationLD().observe(viewLifecycleOwner) { reservations ->
            lifecycleScope.launch {
                checkAndUpdateReservations(reservations)
            }
        }

        handler.post(reservationChecker)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MyPagerAdapter(childFragmentManager)
        binding.tabContent.adapter = adapter
        binding.tab.setupWithViewPager(binding.tabContent)
        binding.btnAddReservation.setOnClickListener{
            findNavController().navigate(
                R.id.action_eventFragment_to_addReservationFragment
            )
        }

    }

    class MyPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> TabPendingReservationFragment.newInstance("Pending", "VIEW")
                1 -> TabPendingReservationFragment.newInstance("Approved", "VIEW")
                2 -> TabPendingReservationFragment.newInstance("History", "VIEW")
                else -> throw Exception()
            }
        }

        override fun getCount(): Int {
            return 3  // Number of tabs
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Pending"
                1 -> "Accepted"
                2 -> "History"
                else -> null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(reservationChecker) // Clean up to avoid memory leaks
    }


    private suspend fun checkAndUpdateReservations(reservations: List<Reservation>) {
        val currentTime = System.currentTimeMillis()

        // First part: Expire old reservations
        val reservationList = reservations // Assuming this returns a list of reservations

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