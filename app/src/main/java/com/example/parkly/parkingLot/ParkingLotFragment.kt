package com.example.parkly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel

class ParkingLotFragment : Fragment() {
    private lateinit var spaceVM: ParkingSpaceViewModel
    private lateinit var parkingLotView: ParkingLotView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        spaceVM = ViewModelProvider(this).get(ParkingSpaceViewModel::class.java)

        // Inflate your view (change R.layout.your_layout to your actual layout resource)
        val view = inflater.inflate(R.layout., container, false)

        // Initialize ParkingLotView and pass the ViewModel
        parkingLotView = ParkingLotView(requireContext(), null, spaceVM)

        // Add your ParkingLotView to the layout
        val layout = view.findViewById<LinearLayout>(R.id.your_linear_layout)
        layout.addView(parkingLotView)

        return view

    }
}
