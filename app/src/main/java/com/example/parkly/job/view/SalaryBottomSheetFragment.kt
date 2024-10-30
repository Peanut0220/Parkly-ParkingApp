package com.example.parkly.job.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.example.parkly.databinding.FragmentSalaryBottomSheetBinding
import com.example.parkly.data.viewmodel.JobViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class SalaryBottomSheetFragment(checkedState: MutableList<String>) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "SalaryBottomSheetFragment"
    }

    private lateinit var binding: FragmentSalaryBottomSheetBinding
    private var listener: BottomSheetListener? = null // Interface reference
    private var type: BottomSheetListener.Type? = null
    private var checkedState = checkedState // Stores checked states
    private val jobVM: JobViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSalaryBottomSheetBinding.inflate(inflater, container, false)

        binding.edtMinSalary.setText(checkedState[0])
        binding.edtMaxSalary.setText(checkedState[1])

        binding.edtMinSalary.doOnTextChanged { text, _, _, _ ->

            val min = text.toString().toIntOrNull()
            val max = binding.edtMaxSalary.text.toString().toIntOrNull()

            jobVM.validateSalaryInput(binding.lblMinSalary, binding.lblMaxSalary, min, max)

        }
        binding.edtMaxSalary.doOnTextChanged { text, _, _, _ ->

            val min = binding.edtMinSalary.text.toString().toIntOrNull()
            val max = text.toString().toIntOrNull()

            jobVM.validateSalaryInput(binding.lblMinSalary, binding.lblMaxSalary, min, max)

        }

        binding.topAppBar.setNavigationOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        var minSalary = binding.edtMinSalary.text.toString().toIntOrNull() ?: 0
        var maxSalary = binding.edtMaxSalary.text.toString().toIntOrNull() ?: 999999

        if (minSalary > maxSalary) {
            minSalary = 0
            maxSalary = 999999
        }

        checkedState[0] = minSalary.toString()
        checkedState[1] = maxSalary.toString()

        listener?.onValueSelected(checkedState, type!!)
    }

    fun setListener(listener: BottomSheetListener, type: BottomSheetListener.Type) {
        this.listener = listener
        this.type = type
    }

}
