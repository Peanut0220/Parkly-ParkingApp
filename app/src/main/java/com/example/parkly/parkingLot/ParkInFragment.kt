package com.example.parkly.parkingLot

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.parkly.R
import com.example.parkly.parkingLot.viewmodel.ParkInViewModel

class ParkInFragment : Fragment() {

    companion object {
        fun newInstance() = ParkInFragment()
    }

    private val viewModel: ParkInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_park_in, container, false)
    }
}