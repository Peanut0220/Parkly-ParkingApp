package com.example.parkly.auth.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.UserActivity
import com.example.parkly.auth.viewmodel.LoginViewModel
import com.example.parkly.databinding.FragmentLoginBinding
import com.example.parkly.util.displayErrorHelper
import com.example.parkly.util.intentWithoutBackstack
import com.example.parkly.util.toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
        var userType = 1; // 1 = employee, 0 = company, testing only
    }

    private val viewModel: LoginViewModel by activityViewModels()
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        buttonAction()



        viewModel.signInResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                requireContext().intentWithoutBackstack(requireActivity(), UserActivity::class.java)
                return@observe
            }

            Snackbar.make(requireView(),
                getString(R.string.please_verify_your_email), Snackbar.LENGTH_LONG)
                .setAction("RESEND") {
                    viewModel.sendEmailVerification()
                }.show()

        }

        viewModel.response.observe(viewLifecycleOwner) { if (it != null) toast(it) }

        return binding.root
    }


    private fun buttonAction() {
        binding.btnLogin.setOnClickListener { signInWithEmail() }

        binding.txtSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.txtForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        binding.btnGoogle.setOnClickListener { signInWithGoogle() }
    }

    private fun signInWithEmail() {
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPassword.text.toString()

        if (!isValid(email, password)) {
            return
        }
        viewModel.firebaseAuthWithEmail(email, password)

    }


    private fun isValid(email: String, password: String): Boolean {
        when {
            email == "" || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                displayErrorHelper(binding.lblEmail, "Invalid email address.")
                return false
            }

            password == "" -> {
                displayErrorHelper(binding.lblPassword, "Wrong password")
                return false
            }

            else -> return true
        }
    }

    /*
    * show Google login dialog
    * onActivityResult() -> if dialog success loaded, auth with google login
    * */
    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, Activity.RESULT_OK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.firebaseAuthWithGoogle(account.idToken!!)

                Log.d("success", "onActivityResult: getID")
            } catch (e: ApiException) {
                Log.d("error", "onActivityResult: ${e.message}")
                toast(getString(R.string.exception_error_msg))
            }
        }
    }


}