package com.example.parkly.auth.view

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.parkly.databinding.FragmentForgotPasswordBinding
import com.example.parkly.util.displayErrorHelper
import com.example.parkly.util.hideErrorHelper
import com.example.parkly.util.toast
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    companion object {
        fun newInstance() = ForgotPasswordFragment()
    }

    private lateinit var binding: FragmentForgotPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)

        binding.topAppBar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.resetBtn.setOnClickListener { resetPassword() }

        return binding.root
    }

    fun resetPassword() {
        val email = binding.edtEmail.text.toString().trim()
        if (!isValid(email)) {
            return
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("Reset password link has been sent to your email.")
                } else {
                    toast(task.exception.toString())
                }
            }
    }

    fun isValid(email: String): Boolean {
        when {
            email == "" || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                displayErrorHelper(binding.txtEmail, "Invalid email address.")
                return false
            }

            else -> {
                hideErrorHelper(binding.txtEmail)
                return true
            }
        }
    }
}