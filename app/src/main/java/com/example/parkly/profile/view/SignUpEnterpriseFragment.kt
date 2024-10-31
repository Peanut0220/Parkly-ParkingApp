package com.example.parkly.profile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.data.Company
import com.example.parkly.data.viewmodel.CompanyViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentSignUpEnterpriseBinding
import com.example.parkly.util.snackbar
import com.example.parkly.util.toast
import com.google.firebase.firestore.Blob
import kotlinx.coroutines.launch

class SignUpEnterpriseFragment : Fragment() {

    lateinit var binding: FragmentSignUpEnterpriseBinding
    private val companyVM: CompanyViewModel by viewModels()
    private val userVM: UserViewModel by viewModels()
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpEnterpriseBinding.inflate(inflater, container, false)

        binding.btnDone.setOnClickListener { submit() }
        binding.topAppBar.setOnClickListener { nav.navigateUp() }
        companyVM.isSuccess.observe(viewLifecycleOwner) {
            if (it) {
                nav.popBackStack(R.id.homeFragment, false)
                snackbar("Company Updated Successfully")
            }
        }
       /* userVM.getUserLD().observe(viewLifecycleOwner) {
            if (it.company_id != "" && it.isEnterprise) {
                updateUI(companyVM.get(it.company_id))
            }
        }*/


        return binding.root
    }

    private fun update(id: String) {
        val company = getInput(id)
        lifecycleScope.launch {
            companyVM.update(company)
        }
    }

    private fun updateUI(company: Company?) {
        if (company != null) {
            binding.edtYear.setText(company.year.toString())
            binding.edtCompanyName.setText(company.name)
            binding.edtLocation.setText(company.location)
            binding.edtCompanyDescription.setText(company.description)
            binding.btnDone.visibility = View.GONE
            binding.btnUpdate.visibility = View.VISIBLE
            binding.btnUpdate.setOnClickListener { update(company.id) }
        }

    }

    private fun submit() {
        val company = getInput()
        if (company.name == "" || company.description == "" || company.location == "" || company.year < 1000) {
            toast("Invalid Input.")
            return
        }

        /*lifecycleScope.launch {
            val companyId = companyVM.set(company)
            userVM.attachCompany(companyId)
        }*/

    }

    private fun getInput(id: String = ""): Company {
        return Company(
            id = id,
            name = binding.edtCompanyName.text.toString(),
            description = binding.edtCompanyDescription.text.toString(),
            location = binding.edtLocation.text.toString(),
            year = binding.edtYear.text.toString().toIntOrNull() ?: -1,
            avatar = userVM.getUserLD().value?.avatar ?: Blob.fromBytes(ByteArray(0))
        )
    }

}