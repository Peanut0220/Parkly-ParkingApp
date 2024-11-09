package com.example.parkly.auth.view

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.auth.viewmodel.SignUpViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentSignUpBinding
import com.example.parkly.util.displayErrorHelper
import com.example.parkly.util.snackbar
import com.example.parkly.util.toast
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {

    private val viewModel: SignUpViewModel by viewModels()
    private val userVM: UserViewModel by viewModels()
    private val nav by lazy { findNavController() }
    private lateinit var binding: FragmentSignUpBinding
    private var type = "Driver"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        binding.btnSignUp.setOnClickListener {
            submit()

        }

        viewModel.errorResponseMsg.observe(viewLifecycleOwner) {
            toast(it)
        }

        viewModel.isSignUpSuccess.observe(viewLifecycleOwner) {
            if (it) {
                lifecycleScope.launch {
                    val user = userVM.getAuth()
                    userVM.set(user)
                }
                nav.navigateUp()
                snackbar(getString(R.string.please_verify_your_email))
            }
        }

        return binding.root
    }

    private fun submit() {
        resetError()
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString()
        val passwordConfirmation = binding.edtPasswordConfirmation.text.toString()

        if (!isValid(email, password, passwordConfirmation)) {
            return
        }

        viewModel.signUpWithEmail(email, password)

    }

    private fun isValid(email: String, password: String, passwordConfirmation: String): Boolean {
        when {

            email == "" || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                displayErrorHelper(binding.lblEmail, "Invalid email address.")
                return false
            }

            password.length < 8 || !password.any { it.isLetter() } || !password.any { it.isDigit() } -> {
                displayErrorHelper(
                    binding.lblPassword,
                    "Password does not fulfill the criteria.\n" + getString(R.string.password_helper_text)
                )
                return false
            }

            password != passwordConfirmation -> {
                displayErrorHelper(binding.lblPasswordConfirm, "Passwords do not match.")
                return false
            }

            else -> return true
        }
    }

    private fun resetError() {
        val edt = listOf(
            binding.lblPassword,
            binding.lblEmail,
            binding.lblPasswordConfirm
        ).forEach { it.error = null }
    }

}