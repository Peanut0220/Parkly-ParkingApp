package com.example.parkly

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.parkly.databinding.FragmentParkingLotBinding
import com.example.parkly.databinding.FragmentParkingSpaceDetailsBinding
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.example.parkly.util.toast

class ParkingLotFragment : Fragment() {
    private lateinit var spaceVM: ParkingSpaceViewModel
    private lateinit var parkingLotView: ParkingLotView
    private val nav by lazy { findNavController() }
    private val action by lazy { arguments?.getString("action") ?: "" }
    private lateinit var binding: FragmentParkingLotBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Use FragmentParkingLotBinding to inflate the layout
        binding = FragmentParkingLotBinding.inflate(inflater, container, false)

        // Initialize the ViewModel
        spaceVM = ViewModelProvider(this).get(ParkingSpaceViewModel::class.java)

        // Handle action-based UI adjustments
        if (action == "reserve") {
            binding.topAppBar.setNavigationOnClickListener {
                nav.navigateUp()
            }
        } else {
            binding.appBar.visibility = View.GONE
            binding.topAppBar.visibility = View.GONE
        }

        // Show or hide the bottom navigation bar based on `action`
        val activity = requireActivity() as UserActivity
        Handler(Looper.getMainLooper()).postDelayed({
            activity.setBottomNavigationVisibility(action != "reserve")
        }, 50)

        // Reference ParkingLotView and set ViewModel
        parkingLotView = binding.parkingLotView
        parkingLotView.setViewModel(spaceVM)

        return binding.root
    }
}

