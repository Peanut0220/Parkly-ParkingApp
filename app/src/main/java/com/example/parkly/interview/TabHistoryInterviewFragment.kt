package com.example.parkly.interview

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
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.InterviewViewModel
import com.example.parkly.data.viewmodel.JobApplicationViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentTabHistoryInterviewBinding
import com.example.parkly.interview.adapter.InterviewHistoryAdapter
import com.example.parkly.data.viewmodel.JobViewModel

import org.joda.time.DateTime

class TabHistoryInterviewFragment : Fragment() {

    companion object {
        fun newInstance() = TabHistoryInterviewFragment()
    }

    private val jobAppVM: JobApplicationViewModel by activityViewModels()
    private val interviewVM: InterviewViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private val companyVM: CompanyViewModel by activityViewModels()
    private val jobVM: JobViewModel by activityViewModels()
    private lateinit var binding: FragmentTabHistoryInterviewBinding
    private val nav by lazy { findNavController() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentTabHistoryInterviewBinding.inflate(inflater, container, false)

        val adapter = InterviewHistoryAdapter( { h, f ->
            h.binding.appliedJob.setOnClickListener {
                nav.navigate(
                    R.id.jobDetailsFragment, bundleOf(
                        "jobID" to f.jobApp.job.jobID
                    )
                )
            }
            h.binding.root.setOnClickListener {
                if (true)
                    nav.navigate(
                        R.id.scheduleInterviewFragment, bundleOf(
                            "jobAppID" to f.jobApp.id,
                            "interviewID" to f.id,
                            "action" to "VIEW"
                        )
                    )
            }
        },true)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        jobVM.getJobsLD().observe(viewLifecycleOwner) {
            interviewVM.updateInterviewList()
        }

        interviewVM.getInterviewLD().observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tabApplicant.visibility = View.INVISIBLE
                binding.tabNoApplicant.visibility = View.VISIBLE
                return@observe
            }
            val interviewHistoryList =
                list.filter {
                    it.date < DateTime.now().minusDays(1)
                        .withTime(23, 59, 59, 999).millis
                }


            interviewHistoryList.forEach { it.jobApp = jobAppVM.get(it.jobAppID)!! }
            interviewHistoryList.forEach { it.jobApp.user = userVM.get(it.jobApp.userId)!! }
            interviewHistoryList.forEach { it.jobApp.job = jobVM.get(it.jobApp.jobId)!! }
            interviewHistoryList.forEach { it.jobApp.job.company = companyVM.get(it.jobApp.job.companyID)!! }

            //filter for the particular user only
            val personalInterviewList = interviewHistoryList.filter {
                if (true)
                    it.jobApp.job.companyID == "wqe"
                else
                    it.jobApp.userId == userVM.getAuth().uid
            }.filter {
                it.jobApp.job.deletedAt == 0L
            }

            if (personalInterviewList.isEmpty()) {
                binding.tabApplicant.visibility = View.INVISIBLE
                binding.tabNoApplicant.visibility = View.VISIBLE
                return@observe
            }


            binding.numApplicant.text = personalInterviewList.size.toString() + " interview(s)"
            binding.tabApplicant.visibility = View.VISIBLE
            binding.tabNoApplicant.visibility = View.GONE


            adapter.submitList(personalInterviewList.sortedByDescending { it.date })

        }


        return binding.root
    }
}