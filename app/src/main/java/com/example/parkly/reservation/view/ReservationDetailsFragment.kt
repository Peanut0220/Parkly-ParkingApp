package com.example.parkly.reservation.view

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.JobApplicationViewModel
import com.example.parkly.data.viewmodel.JobViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentApplicantDetailsBinding
import com.example.parkly.databinding.FragmentReservationDetailsBinding
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.example.parkly.reservation.viewmodel.ReservationViewModel
import com.example.parkly.util.JobApplicationState
import com.example.parkly.util.createChatroom
import com.example.parkly.util.dialog
import com.example.parkly.util.displayDate
import com.example.parkly.util.displayPostTime
import com.example.parkly.util.isChatRoomExist
import com.example.parkly.util.message
import com.example.parkly.util.sendPushNotification
import com.example.parkly.util.snackbar
import com.example.parkly.util.toBitmap
import com.example.parkly.util.toast
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReservationDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = ReservationDetailsFragment()
    }

    private val userVM: UserViewModel by activityViewModels()
    private val reservationVM: ReservationViewModel by activityViewModels()
    private val spaceVM: ParkingSpaceViewModel by activityViewModels()
    private lateinit var binding: FragmentReservationDetailsBinding
    private val nav by lazy { findNavController() }
    private val reservationID by lazy { arguments?.getString("reservationID") ?: "" }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReservationDetailsBinding.inflate(inflater, container, false)

        binding.topAppBar.setNavigationOnClickListener { nav.navigateUp() }


        reservationVM.getreservationLD().observe(viewLifecycleOwner) {
            var reservation = reservationVM.get(reservationID)
            if (reservation == null) {
                nav.navigateUp()
                toast("Reservation Data Is Empty!")
                return@observe
            }

            reservation.user = userVM.get(reservation.userID)!!

            /*
            *
            * Load Applicant Data
            * =======================================
            *
            * */
            binding.applicantName.text = reservation.user.name
            binding.avatarView.loadImage(reservation.user.avatar.toBitmap())
            binding.fileName.text = reservation.file.name
            binding.information.text = if (reservation.reason == "") "-" else reservation.reason
            binding.appliedDate.text = "Applied " + displayPostTime(reservation.createdAt)
            binding.space.text = reservation.spaceID
            binding.lblDate.text = "${displayDate(reservation.date)}"
            binding.startTime.text =
                formatTime(reservation.startTime)
            binding.endTime.text = formatTime(reservation.startTime + reservation.duration)

            binding.file.setOnClickListener {
                nav.navigate(
                    R.id.pdfViewerFragment, bundleOf(
                        "fileName" to reservation.file.name,
                        "url" to reservation.file.path
                    )
                )
            }

            //status color
            when (reservation.status) {
                "Approved", "Rejected", "Cancelled", "Expired","Used" -> {
                    binding.btnReject.visibility = View.GONE
                    binding.btnAccept.visibility = View.GONE
                    binding.status.text = reservation.status
                    when (reservation.status) {
                        "Approved" -> {
                            binding.status.setTextColor(Color.GREEN)
                        }

                        "Rejected" -> {
                            binding.status.setTextColor(Color.RED)
                            binding.btnCancel.visibility = View.GONE
                        }

                        "Cancelled" -> {
                            binding.status.setTextColor(Color.RED)
                            binding.btnCancel.visibility = View.GONE
                        }

                        "Expired" -> {
                            binding.status.setTextColor(Color.RED)
                            binding.btnCancel.visibility = View.GONE
                        }
                        "Used" -> {
                            binding.status.setTextColor(Color.RED)
                            binding.btnCancel.visibility = View.GONE
                        }

                    }
                }

                "Pending" -> {
                    binding.status.text = reservation.status
                    binding.status.setTextColor(Color.rgb(179, 131, 27))
                }

            }


            if (userVM.get(userVM.getAuth().uid)!!.type== "Driver") {
                binding.horizontalScrollView3.visibility = View.VISIBLE
                binding.horizontalScrollView2.visibility = View.GONE
                binding.horizontalScrollView.visibility = View.GONE

                //Cancel button
                binding.btnCancel.setOnClickListener {
                    dialog("Cancel Reservation ", "Are you sure to Cancel?",
                        onPositiveClick = { _, _ ->
                            reservationVM.updateStatus(reservation.id, "Cancelled")

                            snackbar("Your applied reservation for ${reservation.spaceID} has been cancelled.")
                        })
                }

            }else if(userVM.get(userVM.getAuth().uid)!!.type == "Admin"){
                binding.horizontalScrollView3.visibility = View.GONE
                binding.horizontalScrollView2.visibility = View.VISIBLE
                binding.horizontalScrollView.visibility = View.VISIBLE

                //Accept button
                binding.btnAccept.setOnClickListener {
                    dialog("Approve Reservation ", "Are you sure to APPROVE?",
                        onPositiveClick = { _, _ ->
                            reservationVM.updateStatus(reservation.id, "Approved")

                            sendPushNotification(
                                "Reservation APPROVED!",
                                "Your applied reservation for ${reservation.spaceID} has been approved.",
                                reservation.user.token
                            )
                        })
                }

                //Reject button
                binding.btnReject.setOnClickListener {
                    dialog("Reject Reservation", "Are you sure to REJECT?",
                        onPositiveClick = { _, _ ->
                            reservationVM.updateStatus(reservation.id, "Rejected")

                            sendPushNotification(
                                "Opps... Reservation REJECTED.",
                                "Your applied reservation for ${reservation.spaceID} has been rejected.",
                                reservation.user.token
                            )
                        })

                }


                //To send user message
                binding.btnMessage.setOnClickListener {
                    //the ids
                    val userId = userVM.getAuth().uid
                    val otherId = reservation.userID
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
            }else{
                binding.horizontalScrollView3.visibility = View.GONE
                binding.horizontalScrollView2.visibility = View.GONE
                binding.horizontalScrollView.visibility = View.GONE
            }

        }


        return binding.root
    }

    fun formatTime(hour: Int): String {
        val suffix = if (hour >= 12) "PM" else "AM"
        val formattedHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        return "%02d:00 %s".format(formattedHour, suffix)
    }

    //TODO: WAIT FOR SEARCH USER FUNCTION TO TEST


}