package com.example.parkly.profile.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.MainActivity
import com.example.parkly.R
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentSettingBinding
import com.example.parkly.util.intentWithoutBackstack
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class SettingFragment : Fragment() {

    companion object {
        fun newInstance() = SettingFragment()
    }

    private val userVM: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentSettingBinding
    private val auth = Firebase.auth
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        binding.topAppBar.setOnClickListener { nav.navigateUp() }

        binding.cardLogout.setOnClickListener { signOut() }

        binding.cardPassword.setOnClickListener{ nav.navigate(R.id.action_settingFragment_to_changePasswordFragment) }

        binding.cardCompany.setOnClickListener { nav.navigate(R.id.action_settingFragment_to_signUpEnterpriseFragment) }

        binding.cardPersonalInfo.setOnClickListener { nav.navigate(R.id.action_settingFragment_to_profileUpdateFragment) }

        userVM.getUserLD().observe(viewLifecycleOwner) {
            if (it.isEnterprise) {
                binding.cardCompany.visibility = View.VISIBLE
            }
        }

        return binding.root
    }

    private fun signOut() {

        lifecycleScope.launch {
            userVM.setToken("")
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        auth.signOut()

        googleSignInClient.signOut().addOnSuccessListener {
            Log.d("UI", "signOut: navigate to login")

            requireContext().intentWithoutBackstack(requireContext(), MainActivity::class.java)
        }
    }

}