package com.example.parkly.job.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.databinding.FragmentJobDetailsBinding
import com.example.parkly.data.Company
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.JobApplicationViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.data.viewmodel.JobViewModel
import com.example.parkly.util.dialog
import com.example.parkly.util.disable
import com.example.parkly.util.setImageBlob
import com.example.parkly.util.snackbar
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class JobDetailsFragment : Fragment() {

    private lateinit var binding: FragmentJobDetailsBinding
    private val nav by lazy { findNavController() }
    private val jobVM: JobViewModel by activityViewModels()
    private val companyVM: CompanyViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()

    private val jobAppVM: JobApplicationViewModel by activityViewModels()

    private val jobID by lazy { arguments?.getString("jobID") ?: "" }
    private val isArchived by lazy { arguments?.getBoolean("isArchived") ?: false }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentJobDetailsBinding.inflate(inflater, container, false)

        binding.topAppBar.setNavigationOnClickListener {
            nav.navigateUp()
        }

        if (1+1==3) {
            binding.topAppBar.menu.findItem(R.id.edit).isVisible = !isArchived
            binding.topAppBar.menu.findItem(R.id.archive).setVisible(true)
            if (isArchived) {
                binding.topAppBar.menu.findItem(R.id.archive)
                    .setIcon(R.drawable.baseline_unarchive_24)
            }
            binding.btnApply.text = resources.getString(R.string.VIEW_APPLICANT)
            binding.btnApply.setOnClickListener {
                nav.navigate(
                    R.id.action_jobDetailsFragment_to_viewApplicantFragment, bundleOf(
                        "jobID" to jobID
                    )
                )
            }
        } else {
            jobAppVM.getJobAppLD().observe(viewLifecycleOwner) {
                if (jobAppVM.isApplied(userVM.getAuth().uid, jobID)) {
                    binding.btnApply.disable()
                    return@observe
                }

                binding.btnApply.setOnClickListener {
                    nav.navigate(
                        R.id.action_jobDetailsFragment_to_applyJobFragment, bundleOf(
                            "jobID" to jobID
                        )
                    )
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val job = jobVM.get(jobID)!!

        jobVM.getJobsLD().observe(viewLifecycleOwner) { jobList ->
            val job = jobVM.getJobLive(jobList, jobID)

            if (job == null) {
                nav.navigateUp()
                return@observe
            }

            job.company = companyVM.get(job.companyID) ?: Company()

            binding.jobName.text = job.jobName
            binding.companyAvatar.setImageBlob(job.company.avatar)
            binding.companyName.text = job.company.name

        }

        val adapter = MyPagerAdapter(childFragmentManager, job.jobID)
        binding.tabContent.adapter = adapter
        binding.tab.setupWithViewPager(binding.tabContent)
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    detail(job.jobID)
                    true
                }

                R.id.archive -> {
                    if (isArchived) {
                        unarchive(job.jobID)
                        jobVM.updateArchived()
                    } else {
                        archive(job.jobID)
                        jobVM.updateArchived()
                    }
                    true
                }

                else -> false
            }
        }
        /*
        Handler(Looper.getMainLooper()).postDelayed({
            binding.loadingView.visibility = View.GONE
        }, 500)
        */
    }

    private fun unarchive(jobID: String) {
        dialog("Unarchive Job",
            "Are you sure to unarchive this job ? The job will resume accept application.",
            onPositiveClick = { _, _ ->
                val job = jobVM.get(jobID)!!
                job.deletedAt = 0
                lifecycleScope.launch {
                    jobVM.update(job)
                }
                snackbar("Job Unarchived Successfully.")
                nav.navigateUp()
            })
    }

    private fun archive(jobID: String) {
        dialog("Archive Job",
            "Are you sure to archive this job ? The job will not accept application anymore.",
            onPositiveClick = { _, _ ->
                val job = jobVM.get(jobID)!!
                job.deletedAt = DateTime.now().millis
                lifecycleScope.launch {
                    jobVM.update(job)
                }
                snackbar("Job Archived Successfully.")
                nav.navigateUp()
            })
    }

    private fun detail(jobID: String) {
        nav.navigate(
            R.id.postJobFragment, bundleOf(
                "jobID" to jobID
            )
        )
    }

    class MyPagerAdapter(fm: FragmentManager, private val jobID: String) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {

            return when (position) {
                0 -> TabJobDetailsFragment.newInstance(0, jobID)
                1 -> TabJobDetailsFragment.newInstance(1, jobID)
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }

        override fun getCount(): Int {
            return 2  // Number of tabs
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Description"
                1 -> "Company"
                else -> null
            }
        }
    }

}

