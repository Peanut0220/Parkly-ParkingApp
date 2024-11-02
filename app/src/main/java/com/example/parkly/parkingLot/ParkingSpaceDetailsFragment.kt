package com.example.parkly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.parkly.databinding.FragmentParkingSpaceDetailsBinding
import com.example.parkly.databinding.FragmentPostDetailsBinding

class ParkingSpaceDetailsFragment : Fragment() {

    private val spaceID by lazy { requireArguments().getString("spaceID", "") }
    private lateinit var binding: FragmentParkingSpaceDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentParkingSpaceDetailsBinding.inflate(inflater, container, false)
        binding.spaceID.text = spaceID
        return binding.root
    }
}
