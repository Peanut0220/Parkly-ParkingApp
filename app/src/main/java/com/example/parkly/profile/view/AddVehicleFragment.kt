package com.example.parkly.profile.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.community.viewmodel.VehicleViewModel
import com.example.parkly.data.Vehicle
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentAddVehicleBinding
import com.example.parkly.util.convertToLocalMillisLegacy
import com.example.parkly.util.dialog
import com.example.parkly.util.snackbar
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class AddVehicleFragment : Fragment() {

    companion object {
        fun newInstance() = AddVehicleFragment()
    }

    private val vehicleVM: VehicleViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private val nav by lazy { findNavController() }
    private lateinit var binding: FragmentAddVehicleBinding
    private val vehicleID by lazy { arguments?.getString("vehicleID") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddVehicleBinding.inflate(inflater, container, false)

        validateOnTextChanged()

        if(vehicleID != ""){
            var vehicle = vehicleVM.get(vehicleID)
            binding.topAppBar.title ="Edit Vehicle"
            binding.btnPost.text = "Edit"
            if (vehicle != null) {
                binding.edtVehicle.setText(vehicle.vehicleNumber)
                binding.edtVehicleModel.setText(vehicle.vehicleModel)
                binding.btnPost.setOnClickListener { post(true,vehicle) }
            }

        }else{
            binding.btnPost.setOnClickListener { post(false,null) }
        }

        binding.topAppBar.setOnClickListener{
            findNavController().navigateUp()
        }
        return binding.root
    }

    private fun validateOnTextChanged() {
        val textFields = listOf(
            binding.edtVehicle,
            binding.edtVehicleModel

        )

        textFields.forEach { textField ->
            textField.doOnTextChanged { text, _, _, _ ->
                val label = when (textField) {
                    binding.edtVehicle -> binding.lblVehicle
                    binding.edtVehicleModel -> binding.lblVehicleModel

                    else -> null
                }
                vehicleVM.validateInput(label!!, text.toString().trim())
            }
        }


    }


    private fun post(isEditing: Boolean,EditVehicle: Vehicle?) {
        val vehicle = creatVehicleObject(isEditing,EditVehicle)

        if (!isVehicleValid(vehicle)) {
            snackbar("Please fulfill the requirement.")
            return
        }

        if(isEditing){
            dialog("Edit Vehicle", "Are you sure want to submit this edit ?",
                onPositiveClick = { _, _ ->
                    lifecycleScope.launch {
                        vehicleVM.update(vehicle)
                    }

                    snackbar("Vehicle Edited Successfully.")
                    nav.navigateUp()

                })
        }else{
            dialog("Add Vehicle", "Are you sure want to submit this vehicle ?",
                onPositiveClick = { _, _ ->
                    lifecycleScope.launch {
                        vehicleVM.set(vehicle)
                    }

                    snackbar("Vehicle Created Successfully.")
                    nav.navigateUp()
                })


        }}

    private fun creatVehicleObject(isEditing: Boolean,vehicle: Vehicle?): Vehicle {
        if (vehicle != null) {
            Log.d("OK",vehicle.vehicleID)
        }
        return Vehicle(

            vehicleID = if (isEditing) vehicleID else "",
            vehicleNumber = binding.edtVehicle.text.toString().trim(),
            vehicleModel = binding.edtVehicleModel.text.toString().trim(),
            createdAt = if (isEditing) vehicle?.createdAt.toString().toLong() else convertToLocalMillisLegacy(DateTime.now().millis, "Asia/Kuala_Lumpur"),
            userID = userVM.getUserLD().value!!.uid,
            updatedAt = if (isEditing) convertToLocalMillisLegacy(DateTime.now().millis, "Asia/Kuala_Lumpur") else 0,
            deletedAt = 0
        )

    }

    private fun isVehicleValid(vehicle: Vehicle): Boolean {

        val validation = vehicleVM.validateInput(binding.lblVehicle, vehicle.vehicleNumber) && vehicleVM.validateInput(binding.lblVehicleModel, vehicle.vehicleModel)
        return validation
    }
}