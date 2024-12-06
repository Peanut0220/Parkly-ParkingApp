package com.example.parkly

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.parkly.community.viewmodel.VehicleViewModel
import com.example.parkly.data.ParkingRecord
import com.example.parkly.data.ParkingSpace
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.InterviewViewModel
import com.example.parkly.data.viewmodel.JobApplicationViewModel
import com.example.parkly.data.viewmodel.ParkingRecordViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.ActivityUserBinding
import com.example.parkly.job.view.HomeFragment
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.example.parkly.reservation.viewmodel.ReservationViewModel
import com.example.parkly.util.convertToLocalMillisLegacy
import com.example.parkly.util.toast

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.Blob
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

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
    private val clientID =
        "AaSJpijzVdNOolfOOapbud0UQgbBFIYq-_AlYYHptw67I1R1zErBQjaDXflQabiqNkcUznHSaEoW8TVv"
    private val secretID =
        "EOc_hA07fBR2mSQv_tfgbfkIBxIh5EOPZoXFJm4JxSUB3gDAYakwuAIvgFEUpaFTqCqwm3g5z2hw01nE"
    private val returnUrl = "com.example.parkly://paypalpay"

    var accessToken = ""
    private lateinit var uniqueId: String
    private var orderid = ""
    private lateinit var currentRecord:ParkingRecord

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
        AndroidNetworking.initialize(applicationContext)


        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setupNav()
        setContentView(binding.root)
        fetchAccessToken()

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

    private fun handlerOrderID(orderID: String) {
        val config = CoreConfig(clientID, environment = Environment.SANDBOX)
        val payPalWebCheckoutClient = PayPalWebCheckoutClient(this@UserActivity, config, returnUrl)
        payPalWebCheckoutClient.listener = object : PayPalWebCheckoutListener {
            override fun onPayPalWebSuccess(result: PayPalWebCheckoutResult) {
                Log.d(TAG, "onPayPalWebSuccess: $result")
            }

            override fun onPayPalWebFailure(error: PayPalSDKError) {
                Log.d(TAG, "onPayPalWebFailure: $error")
            }

            override fun onPayPalWebCanceled() {
                Log.d(TAG, "onPayPalWebCanceled: ")
            }
        }

        orderid = orderID
        val payPalWebCheckoutRequest =
            PayPalWebCheckoutRequest(orderID, fundingSource = PayPalWebCheckoutFundingSource.PAYPAL)
        payPalWebCheckoutClient.start(payPalWebCheckoutRequest)

    }

    fun startOrder(record: ParkingRecord,totalFee:Double) {
        uniqueId = UUID.randomUUID().toString()
        currentRecord = record
        val orderRequestJson = JSONObject().apply {
            put("intent", "CAPTURE")
            put("purchase_units", JSONArray().apply {
                put(JSONObject().apply {
                    put("reference_id", uniqueId)
                    put("amount", JSONObject().apply {
                        put("currency_code", "MYR")
                        put("value", totalFee.toString())
                    })
                })
            })
            put("payment_source", JSONObject().apply {
                put("paypal", JSONObject().apply {
                    put("experience_context", JSONObject().apply {
                        put("payment_method_preference", "IMMEDIATE_PAYMENT_REQUIRED")
                        put("brand_name", "Parkly")
                        put("locale", "en-US")
                        put("landing_page", "LOGIN")
                        put("shipping_preference", "NO_SHIPPING")
                        put("user_action", "PAY_NOW")
                        put("return_url", returnUrl)
                        put("cancel_url", "https://example.com/cancelUrl")
                    })
                })
            })
        }

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addHeaders("PayPal-Request-Id", uniqueId)
            .addJSONObjectBody(orderRequestJson)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Order Response : " + response.toString())
                    handlerOrderID(response.getString("id"))
                }

                override fun onError(error: ANError) {
                    Log.d(
                        TAG,
                        "Order Error : ${error.message} || ${error.errorBody} || ${error.response}"
                    )
                }
            })
    }

    private fun fetchAccessToken() {
        val authString = "$clientID:$secretID"
        val encodedAuthString = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v1/oauth2/token")
            .addHeaders("Authorization", "Basic $encodedAuthString")
            .addHeaders("Content-Type", "application/x-www-form-urlencoded")
            .addBodyParameter("grant_type", "client_credentials")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    accessToken = response.getString("access_token")
                    Log.d(TAG, accessToken)

                    Toast.makeText(this@UserActivity, "Access Token Fetched!", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onError(error: ANError) {
                    Log.d(TAG, error.errorBody)
                    Toast.makeText(this@UserActivity, "Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: $intent")
        if (intent?.data!!.getQueryParameter("opType") == "payment") {
            captureOrder(orderid)
        } else if (intent?.data!!.getQueryParameter("opType") == "cancel") {
            Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureOrder(orderID: String) {
        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders/$orderID/capture")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addJSONObjectBody(JSONObject()) // Empty body
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Capture Response : " + response.toString())

                    recordVM.updateEndTime(currentRecord.recordID,
                        convertToLocalMillisLegacy(DateTime.now().millis, "Asia/Kuala_Lumpur")
                    ) {
                        // Callback after update is complete
                        val updatedSpace = ParkingSpace(
                            spaceID = currentRecord.spaceID,
                            currentCarImage = Blob.fromBytes(ByteArray(0)),
                            currentRecordID = "",
                            currentUserID = "",
                            spaceStatus = "Available",
                            updatedAt = convertToLocalMillisLegacy(DateTime.now().millis, "Asia/Kuala_Lumpur")
                        )
                        spaceVM.update(updatedSpace)

                        runOnUiThread {
                            Toast.makeText(this@UserActivity, "Payment Successful", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onError(error: ANError) {
                    // Handle the error
                    Log.e(TAG, "Capture Error : " + error.errorDetail)
                }
            })
    }

    companion object {
        const val TAG = "MyTag"
    }



}