package com.example.parkly.profile.view

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.databinding.FragmentChangePasswordBinding
import com.example.parkly.profile.viewmodel.ChangePasswordViewModel
import com.example.parkly.util.dialog
import com.example.parkly.util.disable
import com.example.parkly.util.displayErrorHelper
import com.example.parkly.util.snackbar
import com.example.parkly.util.toast

class ChangePasswordFragment : Fragment() {

    private val nav by lazy { findNavController() }

    private lateinit var binding: FragmentChangePasswordBinding
    private val viewModel: ChangePasswordViewModel by viewModels()
    private var newPassword = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        binding.topAppBar.setOnClickListener { nav.navigateUp() }

        if (!viewModel.isPasswordLogin()){
            binding.btnChangePassword.disable()
            binding.form.visibility = View.INVISIBLE
            binding.google.visibility = View.VISIBLE
        }
        binding.btnChangePassword.setOnClickListener { changePassword() }

        viewModel.response.observe(viewLifecycleOwner) { if (it != null) toast(it) }
        viewModel.isCorrectPassword.observe(viewLifecycleOwner) {
            if (it) {
                dialog(
                    "Change Password",
                    getString(R.string.change_password_confirmation),
                    onPositiveClick = { _, _ ->
                        viewModel.resetPassword(newPassword)
                    }
                )
            }
        }
        viewModel.isSuccess.observe(viewLifecycleOwner) {
            if (it) {
                snackbar("Password Changed Successfully!")
                nav.navigateUp()
            }
        }

        return binding.root

    }

    private fun changePassword() {
        val currentPassword = binding.edtCurrentPassword.text.toString()
        newPassword = binding.edtPassword.text.toString()
        val confirmPassword = binding.edtPasswordConfirmation.text.toString()

        if (currentPassword == "") {
            displayErrorHelper(
                binding.lblCurrentPassword,
                "Please enter current password"
            )
            return
        }

        if (!isValid(newPassword, confirmPassword)) {
            return
        }

        viewModel.isCurrentPasswordValid(currentPassword)

    }


    private fun isValid(password: String, passwordConfirmation: String): Boolean {
        when {
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
}