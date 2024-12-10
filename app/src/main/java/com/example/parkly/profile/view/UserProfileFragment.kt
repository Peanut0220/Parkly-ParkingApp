package com.example.parkly.profile.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.parkly.R
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentUserProfileBinding
import com.example.parkly.profile.tab.TabMyPostListFragment
import com.example.parkly.util.createChatroom
import com.example.parkly.util.isChatRoomExist
import com.example.parkly.util.message
import com.example.parkly.util.snackbar
import com.example.parkly.util.toBitmap
import com.google.android.material.tabs.TabLayoutMediator
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfileFragment : Fragment() {

    companion object {
        private const val REQUEST_CALL_PERMISSION = 1
        fun newInstance() = UserProfileFragment()
    }

    private val userVM: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentUserProfileBinding
    private val userID by lazy { requireArguments().getString("userID", "") }
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        userVM.getUserLLD().observe(viewLifecycleOwner) { userList ->
            val user = userList.find { it.uid == userID } ?: return@observe
            user?.let {
                val avatar =
                    if (it.avatar.toBytes().isEmpty())
                        R.drawable.round_account_circle_24
                    else
                        it.avatar.toBitmap()

                binding.txtName.text = it.name
                binding.avatarView.loadImage(avatar)


            }

            binding.btnMessage.setOnClickListener {

                val userId = userVM.getAuth().uid
                val otherId = user.uid
                var chatRoomId = userId + "_" + otherId

                CoroutineScope(Dispatchers.Main).launch {

                    val isExist = withContext(Dispatchers.IO) {
                        isChatRoomExist(chatRoomId)
                    }


                    if (!isExist) {
                        chatRoomId = otherId + "_" + userId
                        createChatroom(chatRoomId)
                    }

                    message(chatRoomId, nav)
                }
            }

            binding.btnCall.setOnClickListener {
                val phoneNumber = convertToInternationalNumber(user.phone)// Replace with the number you want to call
                makeCall(phoneNumber)

            }

        }




        binding.topAppBar.setOnClickListener {
            findNavController().navigateUp()
        }




        return binding.root
    }

    private fun makeCall(phoneNumber: String) {
        // Check if the CALL_PHONE permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted, initiate the call
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$phoneNumber")
            startActivity(callIntent)
        } else {
            // Request permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can retry the call here if necessary
                Toast.makeText(requireContext(), "Permission granted, retry call", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(requireContext(), "Permission denied to make calls", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun convertToInternationalNumber(localNumber: String): String {
        // Remove all non-digit characters like '-' or spaces
        val cleanedNumber = localNumber.replace(Regex("[^\\d]"), "")

        // Check if the number starts with '0'
        return if (cleanedNumber.startsWith("0")) {
            // Replace the leading '0' with '60'
            "60" + cleanedNumber.substring(1)
        } else {
            // Return the number as it is if no leading '0' (unexpected case)
            cleanedNumber
        }
    }




}
