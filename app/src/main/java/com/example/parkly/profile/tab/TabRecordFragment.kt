package com.example.parkly.profile.tab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.parkly.data.viewmodel.ParkingRecordViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentTabRecordBinding
import com.example.parkly.home.adapter.RecordAdapter

class TabRecordFragment : Fragment() {

    private val userVM: UserViewModel by activityViewModels()
    private val recordVM: ParkingRecordViewModel by activityViewModels()
    lateinit var binding: FragmentTabRecordBinding
    lateinit var adapter: RecordAdapter
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabRecordBinding.inflate(inflater, container, false)

        initAdapter()

        recordVM.getParkingRecordLD().observe(viewLifecycleOwner) { recordList ->
            if (userVM.getUserLD().value == null) return@observe
            var filteredRecordList = recordList.filter { it.userID == userVM.getAuth().uid && it.endTime != 0L}
            if(filteredRecordList.isEmpty()){
                binding.lblNoRecord.visibility = View.VISIBLE
                binding.rv.visibility = View.INVISIBLE
            }else{
                binding.lblNoRecord.visibility = View.GONE
                binding.rv.visibility = View.VISIBLE
            }


            adapter.submitList(filteredRecordList)
        }
        binding.rv.adapter = adapter

        return binding.root
    }

    private fun initAdapter() {
        adapter = RecordAdapter { holder, record ->
           holder.binding.btnPay.visibility = GONE
        binding.rv.adapter = adapter

    }}}