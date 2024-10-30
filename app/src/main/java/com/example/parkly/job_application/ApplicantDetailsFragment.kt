package com.example.parkly.job_application

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.parkly.util.JobApplicationState
import com.example.parkly.util.createChatroom
import com.example.parkly.util.dialog
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

class ApplicantDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = ApplicantDetailsFragment()
    }

    private val userVM: UserViewModel by activityViewModels()
    private val jobAppVM: JobApplicationViewModel by viewModels()
    private val jobVM: JobViewModel by activityViewModels()
    private val companyVM: CompanyViewModel by activityViewModels()
    private lateinit var binding: FragmentApplicantDetailsBinding
    private val nav by lazy { findNavController() }
    private val jobAppID by lazy { arguments?.getString("jobAppID") ?: "" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApplicantDetailsBinding.inflate(inflater, container, false)

        jobAppVM.response.observe(viewLifecycleOwner) { if (it != null) toast(it) }
        jobAppVM.isSuccess.observe(viewLifecycleOwner) { if (it) snackbar("Job Application Status Updated!") }

        binding.topAppBar.setNavigationOnClickListener { nav.navigateUp() }

        jobAppVM.getJobAppLD().observe(viewLifecycleOwner) {
            var jobApp = jobAppVM.get(jobAppID)
            if (jobApp == null) {
                nav.navigateUp()
                toast("Applicant Data Is Empty!")
                return@observe
            }

            jobApp.user = userVM.get(jobApp.userId)!!
            jobApp.job = jobVM.get(jobApp.jobId)!!
            jobApp.job.company = companyVM.get(jobApp.job.companyID)!!

            /*
            *
            * Load Applicant Data
            * =======================================
            *
            * */
            binding.applicantName.text = jobApp.user.name
            binding.avatarView.loadImage(jobApp.user.avatar.toBitmap())
            binding.fileName.text = jobApp.file.name
            binding.information.text = if (jobApp.info == "") "-" else jobApp.info
            binding.appliedDate.text = "Applied " + displayPostTime(jobApp.createdAt)
            binding.chip.text = jobApp.user.email

            binding.chip.setOnClickListener {
                val uri = Uri.parse("mailto:${jobApp.user.email}")
                val i = Intent(Intent.ACTION_SENDTO, uri)
                startActivity(i)
            }

            binding.file.setOnClickListener {
                nav.navigate(
                    R.id.pdfViewerFragment, bundleOf(
                        "fileName" to jobApp.file.name,
                        "url" to jobApp.file.path
                    )
                )
            }

            /*
            * Button Function
            * =======================================
            * */
            when (jobApp.status) {
                JobApplicationState.ACCEPTED.toString() -> {
                    binding.btnAccept.let {
                        it.isClickable = false
                        it.isEnabled = false
                        it.text = jobApp.status
                    }
                    binding.btnInterview.visibility = View.VISIBLE
                    binding.btnInterview.setOnClickListener {
                        nav.navigate(
                            R.id.scheduleInterviewFragment,
                            bundleOf(
                                "jobAppID" to jobAppID
                            )
                        )
                    }
                    binding.btnReject.visibility = View.GONE
                }

                JobApplicationState.REJECTED.toString() -> {
                    binding.btnReject.let {
                        it.isClickable = false
                        it.isEnabled = false
                        it.text = jobApp.status
                    }
                    binding.btnAccept.visibility = View.GONE
                }
            }

            binding.btnAccept.setOnClickListener {
                dialog("Accept Applicant", "Are you sure to ACCEPT ${jobApp.user.name} ?",
                    onPositiveClick = { _, _ ->
                        jobAppVM.updateStatus(
                            JobApplicationState.ACCEPTED,
                            jobAppID
                        )

                        sendPushNotification(
                            "JOB ACCEPTED BY ${jobApp.job.company.name} !",
                            "Your applied job ${jobApp.job.jobName} has been accepted.",
                            jobApp.user.token
                        )
                    })
            }
            binding.btnReject.setOnClickListener {
                dialog("Reject Applicant", "Are you sure to REJECT ${jobApp.user.name} ?",
                    onPositiveClick = { _, _ ->
                        jobAppVM.updateStatus(
                            JobApplicationState.REJECTED,
                            jobAppID
                        )
                        //TODO
                        sendPushNotification(
                            "Opps... JOB REJECTED BY ${jobApp.job.company.name}.",
                            "Your applied job ${jobApp.job.jobName} has been rejected.",
                            jobApp.user.token
                        )
                    })

            }


            binding.btnMessage.setOnClickListener {
                //the ids
                val userId = userVM.getAuth().uid
                val otherId = jobApp.userId
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


        }


        return binding.root
    }

    //TODO: WAIT FOR SEARCH USER FUNCTION TO TEST


}