package com.example.parkly.job_application

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.parkly.R
import com.example.parkly.data.viewmodel.JobApplicationViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentTabApplicantBinding
import com.example.parkly.job_application.adapter.ApplicantAdapter
import com.example.parkly.util.JobApplicationState

class TabApplicantFragment(val jobID: String, val state: JobApplicationState) : Fragment() {


    private val jobAppVM: JobApplicationViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentTabApplicantBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabApplicantBinding.inflate(inflater, container, false)
        when (state) {
            JobApplicationState.NEW -> {
                binding.imgNoApplicant.setImageResource(R.drawable.no_applicant)
                binding.lblNoApplicant.text = getString(R.string.no_applicant)
            }

            JobApplicationState.ACCEPTED-> {
                binding.imgNoApplicant.setImageResource(R.drawable.no_accept_applicant)
                binding.lblNoApplicant.text = getString(R.string.no_accept_applicant)
            }

            JobApplicationState.REJECTED -> {
                binding.imgNoApplicant.setImageResource(R.drawable.no_reject_applicant)
                binding.lblNoApplicant.text = getString(R.string.no_reject_applicant)
            }
        }


        val adapter = ApplicantAdapter { h, f ->
            h.binding.root.setOnClickListener {
                findNavController().navigate(
                    R.id.applicantDetailsFragment,
                    bundleOf("jobAppID" to f.id)
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

        jobAppVM.getJobAppLD().observe(viewLifecycleOwner) { list ->
            val applicantList =
                list.filter { it.jobId == jobID && it.status == state.toString() }

            if (applicantList.isEmpty()) {
                binding.tabApplicant.visibility = View.INVISIBLE
                binding.tabNoApplicant.visibility = View.VISIBLE
                return@observe
            }

            applicantList.forEach { it.user = userVM.get(it.userId)!! }

            binding.numApplicant.text = applicantList.size.toString() + " applicant(s)"
            binding.tabApplicant.visibility = View.VISIBLE
            binding.tabNoApplicant.visibility = View.GONE


            adapter.submitList(applicantList.sortedByDescending { it.createdAt })

        }

        return binding.root
    }


}