package com.example.parkly.reseration.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.parkly.R
import com.example.parkly.data.Reservation
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.InterviewViewModel
import com.example.parkly.data.viewmodel.JobApplicationViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentTabUpcomingInterviewBinding
import com.example.parkly.reservation.adapter.InterviewAdapter
import com.example.parkly.data.viewmodel.JobViewModel
import com.example.parkly.databinding.FragmentTabPendingReservationBinding
import com.example.parkly.reservation.adapter.ReservationAdapter
import com.example.parkly.reservation.viewmodel.ReservationViewModel
import com.example.parkly.util.combineDateTime
import com.example.parkly.util.toast
import org.joda.time.DateTime
import java.lang.Exception
import java.util.Calendar

class TabPendingReservationFragment : Fragment() {

    companion object {
        private const val ARG_STATUS = "status"
        private const val ARG_ACTION = "action"

        fun newInstance(status: String, action: String): TabPendingReservationFragment {
            val fragment = TabPendingReservationFragment()
            fragment.arguments = bundleOf(
                ARG_STATUS to status,
                ARG_ACTION to action
            )
            return fragment
        }
    }

    private val reservationVM: ReservationViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private val jobVM: JobViewModel by activityViewModels()
    private lateinit var binding: FragmentTabPendingReservationBinding
    private val nav by lazy { findNavController() }
    private var filterstatus: String? = null
    private var filteraction: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filterstatus = it.getString(ARG_STATUS)
            filteraction = it.getString(ARG_ACTION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabPendingReservationBinding.inflate(inflater, container, false)






        val adapter = ReservationAdapter { h, f ->
            h.binding.root.setOnClickListener {
                nav.navigate(
                    R.id.action_eventFragment_to_reservationDetailsFragment, bundleOf(
                        "reservationID" to f.id
                    )
                )
            }
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        reservationVM.getreservationLD().observe(viewLifecycleOwner) { list ->
            var reservationList = emptyList<Reservation>()
            reservationList = list.filter { it.userID == userVM.getAuth().uid }
            if (reservationList.isEmpty()) {
                binding.tabReservation.visibility = View.INVISIBLE
                binding.tabNoApplicant.visibility = View.VISIBLE
                return@observe
            }

            if (filterstatus == "Pending") {
                reservationList =
                    list.filter {
                        it.status == "Pending"
                    }
            } else if (filterstatus == "Approved") {
                reservationList =
                    list.filter {
                        it.status == "Approved"
                    }
            } else {
                reservationList =
                    list.filter {
                        it.status != "Approved" &&
                                it.status != "Pending"
                    }
            }

            binding.numApplicant.text = reservationList.size.toString() + " reservation(s)"
            if (reservationList.isEmpty()) {
                binding.tabReservation.visibility = View.INVISIBLE
                binding.tabNoApplicant.visibility = View.VISIBLE
                return@observe
            }


            adapter.submitList(reservationList)


        }

        return binding.root
    }
}