package com.example.parkly.job.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.data.Company
import com.example.parkly.data.SaveJob
import com.example.parkly.data.User
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentHomeBinding
import com.example.parkly.job.adapter.JobAdapter
import com.example.parkly.data.viewmodel.JobViewModel
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.example.parkly.reservation.viewmodel.ReservationViewModel
import com.example.parkly.util.dialogProfileNotComplete
import com.example.parkly.util.getToken
import com.google.android.material.search.SearchView
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.Calendar

class HomeFragment : Fragment(), BottomSheetListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: JobAdapter
    private lateinit var svAdapter: JobAdapter
    private val nav by lazy { findNavController() }
    private val reservationVM: ReservationViewModel by activityViewModels()
    private val spaceVM: ParkingSpaceViewModel by activityViewModels()
    private val jobVM: JobViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private val companyVM: CompanyViewModel by activityViewModels()
    private var chipPositionState = mutableListOf<String>()
    private var chipJobTypeState = mutableListOf<String>()
    private var chipWorkplaceState = mutableListOf<String>()
    private var chipSalaryState = mutableListOf("0", "999999")
    private var isSearching = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.loadingLayout.visibility = View.VISIBLE
        binding.btnSavedJob.visibility = View.VISIBLE
        getGreeting()


        userVM.getUserLD().observe(viewLifecycleOwner) {
            if (it == null) {
                lifecycleScope.launch {
                    userVM.set(userVM.getAuth())
                }
                return@observe
            }

            getToken().observe(viewLifecycleOwner) { token ->
                lifecycleScope.launch {
                    if (userVM.getAuth().token != token) {
                        userVM.setToken(token)
                    }
                }
            }

            binding.username.text = it.name
            var isDialogShown = false
            // Check if the dialog has already been shown
            if (!userVM.isUserDataComplete(it) && !isDialogShown) {
                isDialogShown = true
                dialogProfileNotComplete(nav)
            }






            binding.btnSavedJob.visibility = View.VISIBLE

            //-----------------------------------------------------------
            // Show Job List & Save Job
            adapter = setAdapter(it)
            svAdapter = setAdapter(it)

            binding.rvJobCard.adapter = adapter


            jobVM.updateResult()
            jobVM.reloadJob()
        }

        jobVM.getJobsLD().observe(viewLifecycleOwner) { jobList ->
            if (userVM.getUserLD().value == null) return@observe
            binding.loadingLayout.visibility = View.GONE
            if (jobList.isEmpty()) return@observe

            companyVM.getCompaniesLD().observe(viewLifecycleOwner) { company ->
                if (company != null)
                    jobList.forEach { job ->
                        job.company = companyVM.get(job.companyID) ?: Company()
                    }
            }


            var sortedJobList = jobList.sortedByDescending { job ->
                job.createdAt
            }.filter { it.deletedAt == 0L }


            adapter.submitList(sortedJobList)
        }

        jobVM.getResultLD().observe(viewLifecycleOwner) { jobList ->
            if (userVM.getUserLD().value == null) return@observe
            if (jobList.isEmpty() && !isSearching) return@observe

            companyVM.getCompaniesLD().observe(viewLifecycleOwner) { company ->
                if (company != null)
                    jobList.forEach { job ->
                        job.company = companyVM.get(job.companyID) ?: Company()
                    }
            }


            var sortedJobList = jobList.sortedByDescending { job ->
                job.createdAt
            }


            svAdapter.submitList(sortedJobList)
        }


        //-----------------------------------------------------------
        // Refresh
        binding.refresh.setOnRefreshListener {
            adapter.notifyDataSetChanged()
            svAdapter.notifyDataSetChanged()
            binding.refresh.isRefreshing = false
        }
        //-----------------------------------------------------------
        // Search And Filter




        return binding.root
    }

    private fun setAdapter(it: User): JobAdapter {
        return JobAdapter { holder, job ->
            holder.binding.root.setOnClickListener { detail(job.jobID) }
            if (true) {
                holder.binding.bookmark.visibility = View.VISIBLE
                val saveJob = jobVM.getSaveJobByUser(it.uid)
                saveJob.forEach { jobs ->
                    if (jobs.jobID == job.jobID) {
                        holder.binding.bookmark.isChecked = true
                    }
                }
                holder.binding.bookmark.setOnCheckedChangeListener { _, _ ->
                    val saveJob = SaveJob(
                        id = it.uid + "_" + job.jobID,
                        userID = it.uid,
                        jobID = job.jobID,
                    )
                    if (holder.binding.bookmark.isChecked) {
                        jobVM.saveJob(saveJob)
                    } else {
                        jobVM.unsaveJob(saveJob.id)
                    }
                }
            }
        }
    }

    private fun updateUI() {

       /* if (userVM.isEnterprise()) {
            binding.homeTitle.text = resources.getString(R.string.your_posted_job)
            binding.btnSavedJob.text = resources.getString(R.string.Archived)
            binding.btnSavedJob.setOnClickListener {
                nav.navigate(R.id.action_homeFragment_to_archivedJobFragment)
            }
            binding.btnPostJob.visibility = View.VISIBLE
            binding.btnPostJob.setOnClickListener {
                if (!userVM.isCompanyRegistered()) {
                    dialogCompanyNotRegister(
                        userVM.isEnterprise() && !userVM.isCompanyRegistered(),
                        nav
                    )
                    return@setOnClickListener
                }
                nav.navigate(R.id.action_homeFragment_to_postJobFragment)
            }
        } else {
            binding.homeTitle.text = resources.getString(R.string.recent_job_list)
            binding.btnSavedJob.text = resources.getString(R.string.saved_job)
            binding.btnSavedJob.setOnClickListener {
                nav.navigate(R.id.action_homeFragment_to_savedJobFragment)
            }
        }*/
    }


    private fun clearFilter() {
        chipPositionState = emptyList<String>().toMutableList()
        chipJobTypeState = emptyList<String>().toMutableList()
        chipWorkplaceState = emptyList<String>().toMutableList()
        chipSalaryState = mutableListOf("0", "999999")
        isSearching = false
    }

    private fun chipPosition() {
        val bottomSheetFragment = PositionBottomSheetFragment(chipPositionState)
        bottomSheetFragment.setListener(this, BottomSheetListener.Type.POSITION)
        bottomSheetFragment.show(parentFragmentManager, PositionBottomSheetFragment.TAG)
    }

    private fun chipJobType() {
        val bottomSheetFragment = JobTypeBottomSheetFragment(chipJobTypeState)
        bottomSheetFragment.setListener(this, BottomSheetListener.Type.JOB_TYPE)
        bottomSheetFragment.show(parentFragmentManager, JobTypeBottomSheetFragment.TAG)
    }

    private fun chipWorkplace() {
        val bottomSheetFragment = WorkplaceBottomSheetFragment(chipWorkplaceState)
        bottomSheetFragment.setListener(this, BottomSheetListener.Type.WORKPLACE)
        bottomSheetFragment.show(parentFragmentManager, WorkplaceBottomSheetFragment.TAG)
    }

    private fun chipSalary() {
        val bottomSheetFragment = SalaryBottomSheetFragment(chipSalaryState)
        bottomSheetFragment.setListener(this, BottomSheetListener.Type.SALARY)
        bottomSheetFragment.show(parentFragmentManager, SalaryBottomSheetFragment.TAG)
    }

    private fun detail(jobID: String) {
        nav.navigate(
            R.id.jobDetailsFragment, bundleOf(
                "jobID" to jobID
            )
        )
    }

    private fun getGreeting() {
        val now = DateTime.now().hourOfDay
        when (now) {
            in 0..4 -> binding.lblGreeting.text = resources.getString(R.string.TimeToSleep)
            in 5..11 -> binding.lblGreeting.text = resources.getString(R.string.GoodMorning)
            in 12..17 -> binding.lblGreeting.text = resources.getString(R.string.GoodAfternoon)
            else -> binding.lblGreeting.text = resources.getString(R.string.GoodEvening)
        }
    }

    override fun onValueSelected(value: List<String>, type: BottomSheetListener.Type) {



    }
}


