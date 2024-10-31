package com.example.parkly.job.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.data.Company
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentArchivedJobBinding
import com.example.parkly.job.adapter.ArchivedJobAdapter
import com.example.parkly.data.viewmodel.JobViewModel

class ArchivedJobFragment : Fragment() {

    companion object {
        fun newInstance() = ArchivedJobFragment()
    }

    private val jobVM: JobViewModel by activityViewModels()
    private val companyVM: CompanyViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private val nav by lazy { findNavController() }
    private lateinit var binding: FragmentArchivedJobBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArchivedJobBinding.inflate(inflater, container, false)

        val adapter = ArchivedJobAdapter { holder, job ->
            holder.binding.root.setOnClickListener { detail(job.jobID) }
        }
        binding.rvArchivedJob.adapter = adapter

        jobVM.updateArchived()



        binding.topAppBar.setNavigationOnClickListener { nav.navigateUp() }

        return binding.root
    }

    private fun detail(jobID: String) {
        nav.navigate(
            R.id.jobDetailsFragment, bundleOf(
                "jobID" to jobID,
                "isArchived" to true
            )
        )
    }

}