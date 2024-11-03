package com.example.parkly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
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

        // Initialize the ViewModel
        spaceVM = ViewModelProvider(this).get(ParkingSpaceViewModel::class.java)

        // Inflate your layout
        val view = inflater.inflate(R.layout.fragment_parking_lot, container, false)

        // Reference ParkingLotView by ID and set the ViewModel
        parkingLotView = view.findViewById(R.id.parkingLotView)
        parkingLotView.setViewModel(spaceVM)

        return view

    }
}
