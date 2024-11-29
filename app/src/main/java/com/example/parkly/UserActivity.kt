package com.example.parkly

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.parkly.community.viewmodel.VehicleViewModel
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.InterviewViewModel
import com.example.parkly.data.viewmodel.JobApplicationViewModel
import com.example.parkly.data.viewmodel.ParkingRecordViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.ActivityUserBinding
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.example.parkly.reservation.viewmodel.ReservationViewModel

import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class UserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserBinding
    private val nav by lazy {
        supportFragmentManager.findFragmentById(R.id.user_nav_host)!!.findNavController()
    }
//    private val jobVM: JobViewModel by viewModels()
    private val jobAppVM: JobApplicationViewModel by viewModels()
    private val companyVM: CompanyViewModel by viewModels()
    private val userVM: UserViewModel by viewModels()
    private val spaceVM: ParkingSpaceViewModel by viewModels()
    private val interviewVM: InterviewViewModel by viewModels()
    private val vehicleVM: VehicleViewModel by viewModels()
    private val recordVM: ParkingRecordViewModel by viewModels()
    private val reservationVM: ReservationViewModel by viewModels()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        //Early data loading
        userVM.init()
        companyVM.init()
        jobAppVM.init()
        interviewVM.init()
        spaceVM.init()
        vehicleVM.init()
        recordVM.init()
        reservationVM.init()


        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setupNav()
        setContentView(binding.root)

        // To prevent logout null pointer exception when onPause
        userId = userVM.getAuth().uid
        setOnlineStatus(userId, true)
    }

    private fun setupNav() {
        nav.addOnDestinationChangedListener { _, destination, _ ->

            val hideBottomNavDestinations = setOf(
                R.id.jobDetailsFragment,
                R.id.applyJobFragment,
                R.id.postJobFragment,
                R.id.viewApplicantFragment,
                R.id.applicantDetailsFragment,
                R.id.chatTextFragment,
                R.id.settingFragment,
                R.id.addPostFragment,
                R.id.userProfileFragment,
                R.id.scheduleInterviewFragment,
                R.id.savedJobFragment,
                R.id.archivedJobFragment,
                R.id.signUpEnterpriseFragment,
                R.id.emailVerificationFragment,
                R.id.profileUpdateFragment,
                R.id.pdfViewerFragment,
                R.id.changePasswordFragment,
                R.id.postDetailsFragment,
                R.id.postCommentFragment,
                R.id.parkingSpaceDetailsFragment,
                R.id.parkInFragment,
                R.id.addVehicleFragment,
                R.id.addReservationFragment
            )

            val isBottomNavVisible = !hideBottomNavDestinations.contains(destination.id)

            //TransitionManager.beginDelayedTransition(binding.root as ViewGroup)

            Handler(Looper.getMainLooper()).postDelayed({
                binding.bottomNavigation.visibility =
                    if (isBottomNavVisible) View.VISIBLE else View.GONE
            }, -100)

        }

        binding.bottomNavigation.setupWithNavController(nav)

    }

    fun setBottomNavigationVisibility(isVisible: Boolean) {
        binding.bottomNavigation.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
    fun setOnlineStatus(userId: String, isOnline: Boolean) {
        val onlineStatusRef = FirebaseDatabase.getInstance().getReference("onlineStatus")
        onlineStatusRef.child(userId).get().addOnSuccessListener { snapshot ->
            if (isOnline) {
                onlineStatusRef.child(userId).setValue(true)
            }
            else {
                onlineStatusRef.child(userId).setValue(false)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        setOnlineStatus(userId, true)
    }

    override fun onPause() {
        super.onPause()
        setOnlineStatus(userId, false)
    }


}