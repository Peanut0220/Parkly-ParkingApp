package com.example.parkly.job.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.example.parkly.util.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.data.Job
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentPostJobBinding
import com.example.parkly.data.viewmodel.JobViewModel
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class PostJobFragment : Fragment() {

    companion object {
        fun newInstance() = PostJobFragment()
    }

    private val jobVM: JobViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private val nav by lazy { findNavController() }
    private lateinit var binding: FragmentPostJobBinding
    private val jobID by lazy { arguments?.getString("jobID") ?: "" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentPostJobBinding.inflate(inflater, container, false)

        validateOnTextChanged()

        binding.btnPost.setOnClickListener { post() }

        val isEditing = checkEdit()

        binding.topAppBar.setNavigationOnClickListener { navigateUp(isEditing) }

        return binding.root
    }

    private fun validateOnTextChanged() {
        val textFields = listOf(
            binding.edtJobName,
            binding.edtPosition,
            binding.edtJobType,
            binding.edtWorkplace,
            binding.edtQualification,
            binding.edtExperience,
            binding.edtJobDescription
        )

        textFields.forEach { textField ->
            textField.doOnTextChanged { text, _, _, _ ->
                val label = when (textField) {
                    binding.edtJobName -> binding.lblJobName
                    binding.edtPosition -> binding.lblPosition
                    binding.edtJobType -> binding.lblJobType
                    binding.edtWorkplace -> binding.lblWorkplace
                    binding.edtQualification -> binding.lblQualification
                    binding.edtExperience -> binding.lblExperience
                    binding.edtJobDescription -> binding.lblJobDesc
                    else -> null
                }
                jobVM.validateInput(label!!, text.toString().trim())
            }
        }

        binding.edtMinSalary.doOnTextChanged { text, _, _, _ ->

            val min = text.toString().toIntOrNull()
            val max = binding.edtMaxSalary.text.toString().toIntOrNull()

            jobVM.validateSalaryInput(binding.lblMinSalary, binding.lblMaxSalary, min, max)

        }
        binding.edtMaxSalary.doOnTextChanged { text, _, _, _ ->

            val min = binding.edtMinSalary.text.toString().toIntOrNull()
            val max = text.toString().toIntOrNull()

            jobVM.validateSalaryInput(binding.lblMinSalary, binding.lblMaxSalary, min, max)

        }

    }

    private fun navigateUp(isEditing: Boolean) {

        // Edit
        if (isEditing) {
            if (isJobUnchanged()) {
                nav.popBackStack()
                return
            }
            dialog("Unsaved changes", "Are you sure to discard the changes ?",
                onPositiveClick = { _, _ ->
                    nav.popBackStack()
                }
            )
            return
        }

        // Post
        if (isJobEmpty()) {
            nav.popBackStack()
            return
        }
        dialog("Unsaved changes", "Are you sure to discard the changes ?",
            onPositiveClick = { _, _ ->
                nav.navigateUp()
            }
        )

    }

    private fun post() {
        val job = createJobObject(false)

        if (!isJobDetailsValid(job)) {
            snackbar("Please fulfill the requirement.")
            return
        }

        dialog("Post Job", "Are you sure want to post the job ?",
            onPositiveClick = { _, _ ->
                lifecycleScope.launch {
                    jobVM.set(job)
                }

                snackbar("Job Posted Successfully.")
                nav.navigateUp()

            }
        )
    }

    private fun edit() {
        if (isJobUnchanged()) {
            snackbar("No Changes Made.")
            nav.popBackStack()
            return
        }

        val job = createJobObject(true)

        if (!isJobDetailsValid(job)) {
            snackbar("Please fulfill the requirement.")
            return
        }

        dialog("Edit Job", "Are you sure want to edit the job ?",
            onPositiveClick = { _, _ ->
                lifecycleScope.launch {
                    jobVM.update(job)
                }
                snackbar("Job Edited Successfully.")
                nav.navigateUp()
            }
        )
    }

    private fun createJobObject(isEditing: Boolean): Job {
        return Job(
            jobID = if (isEditing) jobID else "",
            jobName = binding.edtJobName.text.toString().trim(),
            position = binding.edtPosition.text.toString(),
            jobType = binding.edtJobType.text.toString(),
            workplace = binding.edtWorkplace.text.toString(),
            minSalary = binding.edtMinSalary.text.toString().toIntOrNull() ?: -1,
            maxSalary = binding.edtMaxSalary.text.toString().toIntOrNull() ?: -1,
            qualification = binding.edtQualification.text.toString(),
            experience = binding.edtExperience.text.toString().toIntOrNull() ?: -1,
            description = binding.edtJobDescription.text.toString().trim(),
            requirement = binding.edtJobRequirement.text.toString().trim(),
            createdAt = if (isEditing) binding.createdAt.text.toString()
                .toLong() else DateTime.now().millis,
            updatedAt = if (isEditing) DateTime.now().millis else 0,
            companyID = userVM.getUserLD().value!!.company_id
        )

    }

    private fun isJobDetailsValid(job: Job): Boolean {

        val validations = listOf(
            { jobVM.validateInput(binding.lblJobName, job.jobName) },
            { jobVM.validateInput(binding.lblPosition, job.position) },
            { jobVM.validateInput(binding.lblJobType, job.jobType) },
            { jobVM.validateInput(binding.lblWorkplace, job.workplace) },
            {
                jobVM.validateSalaryInput(
                    binding.lblMinSalary,
                    binding.lblMaxSalary,
                    job.minSalary,
                    job.maxSalary
                )
            },
            { jobVM.validateInput(binding.lblQualification, job.qualification) },
            { jobVM.validateInput(binding.lblExperience, job.experience.toString()) },
            { jobVM.validateInput(binding.lblJobDesc, job.description) },
        )

        return validations.all { it() }
    }

    private fun isJobEmpty(): Boolean {

        val validations = listOf(
            { binding.edtJobName.text?.isEmpty() },
            { binding.edtPosition.text?.isEmpty() },
            { binding.edtJobType.text?.isEmpty() },
            { binding.edtWorkplace.text?.isEmpty() },
            { binding.edtMinSalary.text?.isEmpty() },
            { binding.edtMaxSalary.text?.isEmpty() },
            { binding.edtQualification.text?.isEmpty() },
            { binding.edtExperience.text?.isEmpty() },
            { binding.edtJobDescription.text?.isEmpty() },
            { binding.edtJobRequirement.text?.isEmpty() },
        )

        return validations.all { it()!! }
    }

    private fun isJobUnchanged(): Boolean {
        val job = Job(
            jobID = jobID,
            jobName = binding.edtJobName.text.toString().trim(),
            position = binding.edtPosition.text.toString(),
            jobType = binding.edtJobType.text.toString(),
            workplace = binding.edtWorkplace.text.toString(),
            minSalary = binding.edtMinSalary.text.toString().toInt(),
            maxSalary = binding.edtMaxSalary.text.toString().toInt(),
            qualification = binding.edtQualification.text.toString(),
            experience = binding.edtExperience.text.toString().toInt(),
            description = binding.edtJobDescription.text.toString().trim(),
            requirement = binding.edtJobRequirement.text.toString().trim(),
            createdAt = binding.createdAt.text.toString().toLong(),
            updatedAt = binding.updatedAt.text.toString().toLong(),
            companyID = userVM.getUserLD().value!!.company_id
        )
        Log.e("JOB1", job.toString())
        Log.e("JOB2", jobVM.get(jobID).toString())
        return job == jobVM.get(jobID)
    }

    private fun checkEdit(): Boolean {
        val isEditing = jobID.isNotEmpty()

        if (isEditing) {
            val job = jobVM.get(jobID)

            if (job == null) {
                nav.navigateUp()
                return false
            }

            binding.topAppBar.setTitle(R.string.edit_job)
            binding.btnPost.setText(R.string.EDIT)
            binding.btnPost.setOnClickListener { edit() }

            binding.edtJobName.setText(job.jobName)
            binding.edtPosition.setText(job.position, false)
            binding.edtJobType.setText(job.jobType, false)
            binding.edtWorkplace.setText(job.workplace, false)
            binding.edtMinSalary.setText(job.minSalary.toString())
            binding.edtMaxSalary.setText(job.maxSalary.toString())
            binding.edtQualification.setText(job.qualification, false)
            binding.edtExperience.setText(job.experience.toString())
            binding.edtJobDescription.setText(job.description)
            binding.edtJobRequirement.setText(job.requirement)

            binding.createdAt.text = job.createdAt.toString()
            binding.updatedAt.text = job.updatedAt.toString()
            binding.deletedAt.text = job.deletedAt.toString()

        }

        return isEditing
    }

}