package com.example.parkly

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentParkingSpaceDetailsBinding
import com.example.parkly.databinding.FragmentPostDetailsBinding
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel

class ParkingSpaceDetailsFragment : Fragment() {

    private val spaceID by lazy { requireArguments().getString("spaceID", "") }
    private lateinit var binding: FragmentParkingSpaceDetailsBinding
    private val nav by lazy { findNavController() }
    private val spaceVM: ParkingSpaceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentParkingSpaceDetailsBinding.inflate(inflater, container, false)
        binding.topAppBar.setNavigationOnClickListener {
            nav.navigateUp()
        }
        binding.btnParkIn.setOnClickListener { nav.navigate(R.id.action_parkingSpaceDetailsFragment_to_parkInFragment) }
        val space = spaceVM.get(spaceID)
        if (space != null) {
            if(space.spaceStatus == "Available"){
                binding.spaceStatus.text ="Available"
                binding.spaceStatus.setTextColor(Color.GREEN)
            }else if(space.spaceStatus == "Occupied"){
                binding.spaceStatus.text ="Occupied"
                binding.spaceStatus.setTextColor(Color.RED)
            }else if(space.spaceStatus =="Reserved"){
                binding.spaceStatus.text ="Reserved"
                binding.spaceStatus.setTextColor(Color.rgb(179, 131, 27))
            }else{
                binding.spaceStatus.text ="Available"
                binding.spaceStatus.setTextColor(Color.GREEN)
            }
        }


        binding.spaceID.text = spaceID
        return binding.root
    }
}
