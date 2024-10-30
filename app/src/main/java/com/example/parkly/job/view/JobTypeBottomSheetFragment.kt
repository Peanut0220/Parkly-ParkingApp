package com.example.parkly.job.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.parkly.databinding.FragmentJobtypeBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class JobTypeBottomSheetFragment(checkedState: MutableList<String>) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "JobTypeBottomSheetFragment"
    }

    private lateinit var binding: FragmentJobtypeBottomSheetBinding
    private var listener: BottomSheetListener? = null // Interface reference
    private var type: BottomSheetListener.Type? = null
    private var checkedState = checkedState // Stores checked states

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJobtypeBottomSheetBinding.inflate(inflater, container, false)

        binding.chkFulltime.isChecked = checkedState.contains("Full Time")
        binding.chkParttime.isChecked = checkedState.contains("Part Time")
        binding.chkContract.isChecked = checkedState.contains("Contract")
        binding.chkTemporary.isChecked = checkedState.contains("Temporary")
        binding.chkInternship.isChecked = checkedState.contains("Internship")
        binding.chkVolunteer.isChecked = checkedState.contains("Volunteer")

        checkedState.clear()

        binding.topAppBar.setNavigationOnClickListener {
           dismiss()
        }

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (binding.chkFulltime.isChecked) checkedState.add("Full Time")
        if (binding.chkParttime.isChecked) checkedState.add("Part Time")
        if (binding.chkContract.isChecked) checkedState.add("Contract")
        if (binding.chkTemporary.isChecked) checkedState.add("Temporary")
        if (binding.chkInternship.isChecked) checkedState.add("Internship")
        if (binding.chkVolunteer.isChecked) checkedState.add("Volunteer")

        listener?.onValueSelected(checkedState, type!!)
    }

    fun setListener(listener: BottomSheetListener, type: BottomSheetListener.Type) {
        this.listener = listener
        this.type = type
    }

}

