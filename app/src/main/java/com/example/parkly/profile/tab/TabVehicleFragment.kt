package com.example.parkly.profile.tab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.parkly.R
import com.example.parkly.community.viewmodel.VehicleViewModel
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.JobApplicationViewModel
import com.example.parkly.data.viewmodel.JobViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentTabMyJobBinding
import com.example.parkly.databinding.FragmentTabVehicleBinding
import com.example.parkly.profile.adapter.MyJobAdapter
import com.example.parkly.profile.adapter.VehicleAdapter
import com.example.parkly.util.dialog
import com.example.parkly.util.snackbar
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class TabVehicleFragment : Fragment() {

    private val userVM: UserViewModel by activityViewModels()
    private val vehicleVM: VehicleViewModel by activityViewModels()
    lateinit var binding: FragmentTabVehicleBinding
    lateinit var adapter: VehicleAdapter
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabVehicleBinding.inflate(inflater, container, false)

        initAdapter()

        vehicleVM.getVehicleLD().observe(viewLifecycleOwner) { vehicleList ->
            if (vehicleList.isEmpty()) {
                binding.tabNoApplicant.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                return@observe
            }

            val vehicleFilteredList = vehicleList.filter { it.userID == userVM.getAuth().uid }
                .sortedByDescending { it.createdAt }
                .filter { it.deletedAt.toInt() == 0 }

            if (vehicleFilteredList.isEmpty()){
                binding.tabNoApplicant.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                return@observe
            }

            binding.tabNoApplicant.visibility = View.GONE
            binding.rv.visibility = View.VISIBLE

            adapter.submitList(vehicleFilteredList)
        }


        return binding.root
    }

    private fun initAdapter() {
        adapter = VehicleAdapter { holder, vehicle ->
            holder.binding.btnEdit.setOnClickListener {
                // Handle edit action
                findNavController().navigate(
                    R.id.action_profileFragment_to_addVehicleFragment, bundleOf(
                        "vehicleID" to vehicle.vehicleID
                    )
                )
            }
            holder.binding.btnDelete.setOnClickListener {
// Handle delete action
                val oriVehicle = vehicleVM.get(vehicle.vehicleID)
                if (oriVehicle != null) {
                    val updatedVehicle = oriVehicle.copy(deletedAt = DateTime.now().millis)
                    dialog("Delete Vehicle", "Are you sure want to delete this vehicle ?",
                        onPositiveClick = { _, _ ->
                            lifecycleScope.launch {
                                vehicleVM.update(updatedVehicle)
                            }
                            snackbar("Vehicle Deleted Successfully.")
                        })
                }
            }

        }
        binding.rv.adapter = adapter
        binding.rv.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
}}