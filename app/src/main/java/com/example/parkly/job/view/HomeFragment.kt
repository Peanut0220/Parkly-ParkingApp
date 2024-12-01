package com.example.parkly.job.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.parkly.R
import com.example.parkly.data.Company
import com.example.parkly.data.SaveJob
import com.example.parkly.data.User
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentHomeBinding
import com.example.parkly.job.adapter.JobAdapter
import com.example.parkly.data.viewmodel.JobViewModel
import com.example.parkly.data.viewmodel.ParkingRecordViewModel
import com.example.parkly.job.adapter.RecordAdapter
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.example.parkly.profile.adapter.VehicleAdapter
import com.example.parkly.reservation.viewmodel.ReservationViewModel
import com.example.parkly.util.convertToLocalMillisLegacy
import com.example.parkly.util.dialog
import com.example.parkly.util.dialogProfileNotComplete
import com.example.parkly.util.getToken
import com.example.parkly.util.snackbar
import com.google.android.material.search.SearchView
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.Calendar

class HomeFragment : Fragment(), BottomSheetListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: RecordAdapter
    private lateinit var svAdapter: JobAdapter
    private val nav by lazy { findNavController() }
    private val reservationVM: ReservationViewModel by activityViewModels()
    private val recordVM: ParkingRecordViewModel by activityViewModels()
    private val spaceVM: ParkingSpaceViewModel by activityViewModels()
    private val jobVM: JobViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private val companyVM: CompanyViewModel by activityViewModels()
    private var isSearching = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.loadingLayout.visibility = View.VISIBLE

        getGreeting()
setAdapter()

        userVM.getUserLD().observe(viewLifecycleOwner) {
            if (it == null) {
                lifecycleScope.launch {
                    userVM.set(userVM.getAuth())
                }
                return@observe
            }

            getToken().observe(viewLifecycleOwner) { token ->
                lifecycleScope.launch {
                    if (userVM.getAuth().token != token) {
                        userVM.setToken(token)
                    }
                }
            }

            binding.username.text = it.name
            var isDialogShown = false
            // Check if the dialog has already been shown
            if (!userVM.isUserDataComplete(it) && !isDialogShown) {
                isDialogShown = true
                dialogProfileNotComplete(nav)
            }


            recordVM.getParkingRecordLD().observe(viewLifecycleOwner) { recordList ->
                if (userVM.getUserLD().value == null) return@observe
                binding.loadingLayout.visibility = View.GONE
                var filteredRecordList = recordList.filter { it.userID == userVM.getAuth().uid && it.endTime == 0L}
                if (filteredRecordList.isEmpty()) return@observe

                adapter.submitList(filteredRecordList)
            }

            binding.rv.adapter = adapter


        }


        //-----------------------------------------------------------
        // Refresh
        binding.refresh.setOnRefreshListener {
            adapter.notifyDataSetChanged()
            binding.refresh.isRefreshing = false
        }
        //-----------------------------------------------------------
        // Search And Filter


        return binding.root
    }

    private fun setAdapter() {
        adapter = RecordAdapter { holder, record ->
            holder.binding.btnPay.setOnClickListener {
                // Handle edit action
                findNavController().navigate(
                    R.id.action_profileFragment_to_addVehicleFragment
                )
            }

        }
        binding.rv.adapter = adapter
    }

    private fun updateUI() {

    }

    private fun getGreeting() {
        val now = DateTime.now().hourOfDay
        when (now) {
            in 0..4 -> binding.lblGreeting.text = resources.getString(R.string.TimeToSleep)
            in 5..11 -> binding.lblGreeting.text = resources.getString(R.string.GoodMorning)
            in 12..17 -> binding.lblGreeting.text = resources.getString(R.string.GoodAfternoon)
            else -> binding.lblGreeting.text = resources.getString(R.string.GoodEvening)
        }
    }

    override fun onValueSelected(value: List<String>, type: BottomSheetListener.Type) {



    }
}


