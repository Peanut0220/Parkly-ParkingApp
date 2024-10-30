package com.example.parkly.job.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.parkly.data.Company
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.databinding.FragmentTabJobDetailsBinding
import com.example.parkly.data.viewmodel.JobViewModel


class TabJobDetailsFragment : Fragment() {

    companion object {
        private const val ARG_POSITION = "position"
        private const val ARG_JOBID = "jobID"

        fun newInstance(position: Int, jobID: String): TabJobDetailsFragment {
            val fragment = TabJobDetailsFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            args.putString(ARG_JOBID, jobID)
            fragment.arguments = args
            return fragment
        }
    }

    private val jobVM: JobViewModel by activityViewModels()
    private val companyVM: CompanyViewModel by activityViewModels()
    private lateinit var binding: FragmentTabJobDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentTabJobDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val position = arguments?.getInt(ARG_POSITION, 0) ?: 0
        val jobID = arguments?.getString(ARG_JOBID) ?: ""

        jobVM.getJobsLD().observe(viewLifecycleOwner) { jobList ->
            val job = jobVM.getJobLive(jobList, jobID)
            job.company = companyVM.get(job.companyID) ?: Company()

            binding.lblJobDesc.text = job.description
            binding.lblRequirements.text = if (job.requirement == "") "-" else job.requirement
            binding.lblPosition.text = job.position
            binding.lblJobType.text = job.jobType
            binding.lblWorkplace.text = job.workplace
            binding.lblSalary.text = "RM ${job.minSalary} - RM ${job.maxSalary} per month"
            binding.lblQualification.text = job.qualification
            binding.lblExperience.text = "${job.experience} year(s)"
            binding.lblAboutCompany.text = job.company.description
            binding.lblLocation.text = job.company.location
            binding.lblSince.text = job.company.year.toString()
        }

        when (position) {
            0 -> {
                binding.tabDesc.visibility = View.VISIBLE
                binding.tabCompany.visibility = View.GONE
            }
            1 -> {
                binding.tabCompany.visibility = View.VISIBLE
                binding.tabDesc.visibility = View.GONE
            }
        }
    }



}