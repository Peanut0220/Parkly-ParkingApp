package com.example.parkly.profile.view

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.parkly.profile.viewmodel.EmailVerificationViewModel
import com.example.parkly.R
import com.example.parkly.databinding.FragmentEmailVerificationBinding
import com.example.parkly.util.snackbar
import com.example.parkly.util.toast

class EmailVerificationFragment : Fragment() {

    companion object {
        fun newInstance() = EmailVerificationFragment()
    }

    private val viewModel: EmailVerificationViewModel by viewModels()
    lateinit var binding: FragmentEmailVerificationBinding
    private val nav by lazy { findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.sendEmailVerification()
        viewModel.checkEmailVerificationInterval()


        viewModel.isVerified.observe(this) {
            if (it) {
                nav.navigate(R.id.homeFragment)
                snackbar("Email Successfully Verified! You can enjoy the full features of LaunchPad now!")
            }
        }

        viewModel.errorResponseMsg.observe(this) {
            if (it != null) toast("Please check your email.")
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
        binding.txtSentTo.text = """
            An email verification sent to 
            ${viewModel.auth.currentUser?.email}
        """.trimIndent()
        binding.btnResend.setOnClickListener { viewModel.sendEmailVerification() }
        binding.topAppBar.setOnClickListener { nav.navigateUp() }
        return binding.root
    }
}