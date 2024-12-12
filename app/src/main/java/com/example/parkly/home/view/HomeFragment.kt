package com.example.parkly.home.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.UserActivity
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentHomeBinding
import com.example.parkly.data.viewmodel.ParkingRecordViewModel
import com.example.parkly.home.adapter.RecordAdapter
import com.example.parkly.util.convertToLocalMillisLegacy
import com.example.parkly.util.dialogProfileNotComplete
import com.example.parkly.util.getToken

import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment(), BottomSheetListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: RecordAdapter
    private val nav by lazy { findNavController() }

    private val recordVM: ParkingRecordViewModel by activityViewModels()

    private val userVM: UserViewModel by activityViewModels()



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
                if(filteredRecordList.isEmpty()){
                    binding.noRecord.visibility = View.VISIBLE
                    binding.rv.visibility = View.INVISIBLE
                    binding.homeTitle.visibility = View.INVISIBLE
                }else{
                    binding.noRecord.visibility = View.GONE
                    binding.rv.visibility = View.VISIBLE
                    binding.homeTitle.visibility = View.VISIBLE
                }


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
            val startTimeMillis = record.startTime // Assuming this is in milliseconds
            val date = Date(startTimeMillis) // Convert to Date object
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val formattedTime = formatter.format(date)

// Calculate End Time
            val endTimeMillis = if (record.endTime == 0L) {
                // Use current time if endTime is not available
                convertToLocalMillisLegacy(DateTime.now().millis, "Asia/Kuala_Lumpur")
            } else {
                record.endTime
            }

// Calculate the difference in milliseconds (ensure non-negative)
            val durationMillis = (endTimeMillis - startTimeMillis).coerceAtLeast(0L)

// Convert milliseconds to hours and minutes
            val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
            val hours = totalMinutes / 60 // Full hours
            val minutes = totalMinutes % 60 // Remaining minutes

// Define the rates
            val firstHourRate = 2.00
            val subsequentHourRate = 5.00

// Calculate the total fee
            var totalFee = if (hours == 0L && minutes > 0||( hours == 0L && minutes.toInt() == 0)) {
                firstHourRate // Charge for the first hour if it's less than 1 hour
            } else {
                firstHourRate + (hours - 1).coerceAtLeast(0L) * subsequentHourRate + if (minutes > 0) subsequentHourRate else 0.0
            }

recordVM.updateAmount(record.recordID,totalFee)
            holder.binding.btnPay.setOnClickListener {
                (activity as? UserActivity)?.startOrder(record, totalFee)
            }
        }

        binding.rv.adapter = adapter
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


