package com.example.parkly

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.parkly.community.viewmodel.VehicleViewModel
import com.example.parkly.data.viewmodel.ParkingRecordViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentParkingSpaceDetailsBinding
import com.example.parkly.databinding.FragmentPostDetailsBinding
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.example.parkly.reservation.viewmodel.ReservationViewModel
import com.example.parkly.util.setImageBlob
import com.example.parkly.util.toBitmap
import io.getstream.avatarview.coil.loadImage
import java.util.Calendar

class ParkingSpaceDetailsFragment : Fragment() {

    private val spaceID by lazy { requireArguments().getString("spaceID", "") }
    private lateinit var binding: FragmentParkingSpaceDetailsBinding
    private val nav by lazy { findNavController() }
    private val spaceVM: ParkingSpaceViewModel by activityViewModels()
    private val recordVM: ParkingRecordViewModel by activityViewModels()
    private val reservationVM: ReservationViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentParkingSpaceDetailsBinding.inflate(inflater, container, false)
        binding.topAppBar.setNavigationOnClickListener {
            nav.navigateUp()
        }
        binding.btnParkIn.setOnClickListener {
            findNavController().navigate(
                R.id.action_parkingSpaceDetailsFragment_to_parkInFragment, bundleOf(
                    "spaceID" to binding.spaceID.text
                )
            )
        }
        val space = spaceVM.get(spaceID)

        if (space != null) {
            Log.d("OK",""+space.currentRecordID)
            Log.d("OK",""+recordVM.get(space.currentRecordID))
            if(space.spaceStatus == "Available"){
                binding.spaceStatus.text ="Available"
                binding.spaceStatus.setTextColor(Color.GREEN)
                binding.btnViewProfile.visibility = GONE
                binding.carImage.visibility = GONE
            }else if(space.spaceStatus == "Occupied"){
                binding.spaceStatus.text ="Occupied"
                binding.spaceStatus.setTextColor(Color.RED)
                binding.carImage.setImageBlob( space.currentCarImage)
                binding.currentVehicle.text =recordVM.get(space.currentRecordID)?.vehicleNumber
                binding.btnViewProfile.setOnClickListener {  findNavController().navigate(
                    R.id.action_parkingSpaceDetailsFragment_to_userProfileFragment, bundleOf(
                        "userID" to space.currentUserID
                    )
                )}
                binding.btnParkIn.isEnabled = false
            }else if (space.spaceStatus == "Reserved") {
                binding.spaceStatus.text = "Reserved"
                binding.spaceStatus.setTextColor(Color.rgb(179, 131, 27))
                binding.btnViewProfile.visibility = GONE
                binding.carImage.visibility = GONE

                val currentTime = System.currentTimeMillis()
                val currentReservation = reservationVM.getBySpaceID(spaceID) // Fetch reservation by spaceID

                if (currentReservation != null && currentReservation.status == "Approved" && currentReservation.userID == userVM.getAuth().uid) {
                    // Combine date and start time to calculate the reservation start time
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = currentReservation.date // Reservation date
                    calendar.add(Calendar.HOUR_OF_DAY, currentReservation.startTime) // Add start time (in hours)
                    val reservationStartTime = calendar.timeInMillis

                    // Calculate the reservation end time
                    calendar.add(Calendar.HOUR_OF_DAY, currentReservation.duration)
                    val reservationEndTime = calendar.timeInMillis

                    // Check if current time is within 1 hour before the start time until the end time
                    val oneHourBeforeStartTime = reservationStartTime - (60 * 60 * 1000) // 1 hour in milliseconds
                    if (currentTime in oneHourBeforeStartTime until reservationEndTime
                    ) {
                        binding.btnParkIn.isEnabled = true // Enable button if the user made the reservation
                        binding.cannotPark.visibility = INVISIBLE
                    } else {
                        binding.btnParkIn.isEnabled = false // Disable button otherwise
                        binding.cannotPark.visibility = VISIBLE
                        binding.cannotPark.text = "You cannot park here at this time."
                    }
                } else {
                    binding.btnParkIn.isEnabled = false // Disable button if reservation is not approved
                    binding.cannotPark.visibility = VISIBLE
                    binding.cannotPark.text = "This space is reserved by another user."
                }
            }else{
                binding.spaceStatus.text ="Available"
                binding.spaceStatus.setTextColor(Color.GREEN)
                binding.btnViewProfile.visibility = GONE
                binding.carImage.visibility = GONE
            }
        }

        //if reservation


        val latestRecords = recordVM.getLatestByUser(userVM.getAuth().uid)
        if (latestRecords != null) {
            // No ParkingRecord found, go to the first condition
            binding.btnParkIn.isEnabled = false
            binding.cannotPark.visibility = VISIBLE
        }
        if(latestRecords?.spaceID == spaceID){
            binding.cannotPark.text = "You have already parked in this spot"
        }


        binding.spaceID.text = spaceID
        return binding.root
    }
}
