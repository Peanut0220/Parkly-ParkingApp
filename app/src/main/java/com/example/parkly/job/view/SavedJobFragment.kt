package com.example.parkly.job.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.data.Company
import com.example.parkly.data.Job
import com.example.parkly.data.SaveJob
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentSavedJobBinding
import com.example.parkly.job.adapter.SavedJobAdapter
import com.example.parkly.data.viewmodel.JobViewModel

class SavedJobFragment : Fragment() {

    companion object {
        fun newInstance() = SavedJobFragment()
    }

    private val jobVM: JobViewModel by activityViewModels()
    private val companyVM: CompanyViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private val nav by lazy { findNavController() }
    private lateinit var binding: FragmentSavedJobBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSavedJobBinding.inflate(inflater, container, false)

        val uid = userVM.getAuth().uid

        val adapter = SavedJobAdapter { holder, job ->
            holder.binding.root.setOnClickListener { detail(job.jobID) }
            holder.binding.bookmark.visibility = View.VISIBLE
            val saveJob = jobVM.getSaveJobByUser(uid)

            saveJob.forEach {
                if (it.jobID == job.jobID) {
                    holder.binding.bookmark.isChecked = true
                }
            }
            holder.binding.bookmark.setOnCheckedChangeListener { _, _ ->
                val saveJob = SaveJob(
                    id = uid + "_" + job.jobID,
                    userID = uid,
                    jobID = job.jobID,
                )
                if (holder.binding.bookmark.isChecked) {
                    jobVM.saveJob(saveJob)
                } else {
                    jobVM.unsaveJob(saveJob.id)
                }
            }

        }
        binding.rvSavedJob.adapter = adapter

        jobVM.getSaveJobLD().observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.noSaveJob.visibility = View.VISIBLE
            }
            it.forEach {
                it.job = jobVM.get(it.jobID) ?: Job()
                it.job.company = companyVM.get(it.job.companyID) ?: Company()
            }
            adapter.submitList(it.sortedByDescending { it.job.createdAt })
        }

        binding.topAppBar.setNavigationOnClickListener { nav.navigateUp() }

        return binding.root
    }

    private fun detail(jobID: String) {
        nav.navigate(
            R.id.jobDetailsFragment, bundleOf(
                "jobID" to jobID
            )
        )
    }


}