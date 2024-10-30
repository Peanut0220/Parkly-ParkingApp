package com.example.parkly.profile.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.data.User
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentProfileUpdateBinding
import com.example.parkly.util.cropToBlob
import com.example.parkly.util.snackbar
import com.example.parkly.util.toBitmap
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.launch

class ProfileUpdateFragment : Fragment() {

    lateinit var binding: FragmentProfileUpdateBinding
    val userVM: UserViewModel by viewModels()
    val companyVM: CompanyViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileUpdateBinding.inflate(inflater, container, false)

        binding.topAppBar.setOnClickListener { findNavController().navigateUp() }

        binding.avatar.setOnClickListener { getContent.launch("image/*") }

        binding.btnDone.setOnClickListener { submit() }

        userVM.getUserLD().observe(viewLifecycleOwner) { user ->
            binding.edtEmail.setText(user.email)
            binding.edtName.setText(user.name)
            val avatar =
                if (user.avatar.toBytes().isEmpty())
                    R.drawable.round_account_circle_24
                else
                    user.avatar.toBitmap()

            binding.avatar.loadImage(avatar)
        }

        userVM.response.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(R.id.profileFragment)
                snackbar("Profile updated successfully!")
            }
        }

        return binding.root
    }

    private fun submit() {
        val name = binding.edtName.text.toString().trim()
        val avatar = binding.avatar.cropToBlob(300, 300)

        if (name == "") {
            return
        }

        lifecycleScope.launch {
            userVM.update(User(name = name, avatar = avatar))
            if (userVM.isEnterprise()) {
                companyVM.syncAvatar(userVM.getUserLD().value?.company_id, avatar)
            }
        }

    }

    // Get-content launcher
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) return@registerForActivityResult
        binding.avatar.loadImage(it)
    }

}