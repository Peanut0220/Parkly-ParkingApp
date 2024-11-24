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
        // Use FragmentParkingLotBinding to inflate the layout
        binding = FragmentParkingLotBinding.inflate(inflater, container, false)

        // Initialize the ViewModel
        spaceVM = ViewModelProvider(this).get(ParkingSpaceViewModel::class.java)
        reservationVM = ViewModelProvider(this).get(ReservationViewModel::class.java)
        userVM = ViewModelProvider(this).get(UserViewModel::class.java)

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

        val parkingLotView = binding.parkingLotView

        // Pass ViewModel and LifecycleOwner to ParkingLotView
        parkingLotView.setViewModel(spaceVM, reservationVM,userVM)
        parkingLotView.setLifecycleOwner(viewLifecycleOwner)

        // Pass additional parameters to ParkingLotView
        parkingLotView.setParameters(action, date, startTime, duration, fileUri, reason,fileName)



        return binding.root
    }

}


