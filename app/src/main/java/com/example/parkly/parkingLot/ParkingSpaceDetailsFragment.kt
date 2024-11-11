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
import com.example.parkly.util.setImageBlob
import com.example.parkly.util.toBitmap
import io.getstream.avatarview.coil.loadImage

class ParkingSpaceDetailsFragment : Fragment() {

    private val spaceID by lazy { requireArguments().getString("spaceID", "") }
    private lateinit var binding: FragmentParkingSpaceDetailsBinding
    private val nav by lazy { findNavController() }
    private val spaceVM: ParkingSpaceViewModel by activityViewModels()
    private val recordVM: ParkingRecordViewModel by activityViewModels()
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
            }else if(space.spaceStatus =="Reserved"){
                binding.spaceStatus.text ="Reserved"
                binding.spaceStatus.setTextColor(Color.rgb(179, 131, 27))
                binding.btnViewProfile.visibility = GONE
                binding.carImage.visibility = GONE
                binding.btnParkIn.isEnabled = false
            }else{
                binding.spaceStatus.text ="Available"
                binding.spaceStatus.setTextColor(Color.GREEN)
                binding.btnViewProfile.visibility = GONE
                binding.carImage.visibility = GONE
            }
        }

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
