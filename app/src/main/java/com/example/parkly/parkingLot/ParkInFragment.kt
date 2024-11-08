package com.example.parkly.parkingLot

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.community.viewmodel.VehicleViewModel
import com.example.parkly.data.ParkingRecord
import com.example.parkly.data.ParkingSpace
import com.example.parkly.data.User
import com.example.parkly.data.Vehicle
import com.example.parkly.data.viewmodel.ParkingRecordViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentParkInBinding
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.example.parkly.util.cropToBlob
import com.example.parkly.util.dialog
import com.example.parkly.util.snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.launch
import org.joda.time.DateTime


class ParkInFragment : Fragment() {

    private lateinit var captureButton: Button
    private lateinit var capturedImageView: ImageView
    private val CAMERA_REQUEST_CODE = 100
    private lateinit var binding: FragmentParkInBinding
    private val vehicleVM: VehicleViewModel by activityViewModels()
    private val recordVM: ParkingRecordViewModel by activityViewModels()
    private val spaceVM: ParkingSpaceViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private val nav by lazy { findNavController() }
    private val spaceID by lazy { requireArguments().getString("spaceID", "") }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParkInBinding.inflate(inflater, container, false)

        binding.btnAdd.setOnClickListener {
            findNavController().navigate(
                R.id.action_parkInFragment_to_addVehicleFragment, bundleOf(
                    "vehicleID" to ""
                )
            )
        }
        binding.topAppBar.setNavigationOnClickListener {
            nav.navigateUp()
        }
        binding.btnConfirm.setOnClickListener { submit() }


        loadSpinnerData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        captureButton = view.findViewById(R.id.captureButton)
        capturedImageView = view.findViewById(R.id.capturedImageView)

        captureButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            capturedImageView.setImageBitmap(photo)
            capturedImageView.visibility = View.VISIBLE
            labelImage(photo)
        }
    }

    private fun labelImage(photo: Bitmap) {
        val inputImage = InputImage.fromBitmap(photo, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        labeler.process(inputImage)
            .addOnSuccessListener { labels ->
                var carFrontDetected = false

                for (label in labels) {
                    // Check if detected labels include car-related frontal features
                    if ((label.text.equals("Car", ignoreCase = true) &&
                        label.text.contains("Headlight", ignoreCase = true)) ||
                        label.text.contains("Bumper", ignoreCase = true)) {

                        // Adjust confidence as needed
                        if (label.confidence > 0.84) {
                            carFrontDetected = true
                            break
                        }
                    }
                }

                if (carFrontDetected) {
                    Toast.makeText(requireContext(), "Car front detected, proceed", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No car front detected, please take another photo", Toast.LENGTH_SHORT).show()
                    capturedImageView.setImageBitmap(null)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSpinnerData() {

        vehicleVM.getVehicleLD().observe(viewLifecycleOwner) { vehicleList ->
            var filteredVehicleList = vehicleList.filter { it.userID == userVM.getAuth().uid }
                .filter { it.deletedAt == 0L }
                .sortedByDescending { it.createdAt }
                .map { it.vehicleNumber }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filteredVehicleList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinner.adapter = adapter
        }
    }

    private fun submit() {
        val record = createRecordObject()


        if (binding.capturedImageView ==null) {
            snackbar("Please capture your car.")
            return
        }

            dialog("Park In", "Are you sure want to park in?",
                onPositiveClick = { _, _ ->
                    lifecycleScope.launch {
                        recordVM.set(record)
                        val space = updateSpaceObject()
                        spaceVM.update(space)
                    }


                    snackbar("Park In Successfully.")
                    findNavController().navigate(
                        R.id.homeFragment
                    )
                })


        }

    private fun createRecordObject(): ParkingRecord {
        return ParkingRecord(
            recordID = "",
            spaceID = spaceID,
            userID = userVM.getUserLD().value!!.uid,
            startTime = DateTime.now().millis,
            endTime = 0,
            carImage = binding.capturedImageView.cropToBlob(binding.capturedImageView.getDrawable().getIntrinsicWidth(),binding.capturedImageView.getDrawable().getIntrinsicHeight()),
            vehicleNumber = binding.spinner.selectedItem.toString()
        )

    }

    private fun updateSpaceObject(): ParkingSpace {
        return ParkingSpace(
            spaceID = spaceID,
            currentCarImage = binding.capturedImageView.cropToBlob(binding.capturedImageView.getDrawable().getIntrinsicWidth(),binding.capturedImageView.getDrawable().getIntrinsicHeight()),
            currentRecordID = recordVM.getLatestBySpace(spaceID)?.recordID ?: "",
            spaceStatus = "Occupied",
            currentUserID = userVM.getUserLD().value!!.uid,
            updatedAt = DateTime.now().millis
        )
    }


}
