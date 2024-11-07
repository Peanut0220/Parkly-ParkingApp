package com.example.parkly.parkingLot

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.databinding.FragmentParkInBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions


class ParkInFragment : Fragment() {

    private lateinit var captureButton: Button
    private lateinit var capturedImageView: ImageView
    private val CAMERA_REQUEST_CODE = 100
    private lateinit var binding: FragmentParkInBinding
    private lateinit var imageUri: Uri
    private val nav by lazy { findNavController() }

    // Create the object detector with default options
    private val objectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE) // Use streaming mode for better performance
            .enableMultipleObjects() // Enable detection of multiple objects
            .build()

        ObjectDetection.getClient(options)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParkInBinding.inflate(inflater, container, false)


        binding.topAppBar.setNavigationOnClickListener {
            nav.navigateUp()
        }
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
                        if (label.confidence > 0.85) {
                            carFrontDetected = true
                            break
                        }
                    }
                }

                if (carFrontDetected) {
                    Toast.makeText(requireContext(), "Car front detected, proceed", Toast.LENGTH_SHORT).show()
                    // Proceed with the next steps
                } else {
                    Toast.makeText(requireContext(), "No car front detected, please take another photo", Toast.LENGTH_SHORT).show()
                    capturedImageView.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



}
