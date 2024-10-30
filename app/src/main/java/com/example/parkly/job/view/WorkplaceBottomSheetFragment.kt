package com.example.parkly.job.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.parkly.databinding.FragmentWorkplaceBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class WorkplaceBottomSheetFragment(checkedState: MutableList<String>) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "WorkplaceBottomSheetFragment"
    }

    private lateinit var binding: FragmentWorkplaceBottomSheetBinding
    private var listener: BottomSheetListener? = null // Interface reference
    private var type: BottomSheetListener.Type? = null
    private var checkedState = checkedState // Stores checked states

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWorkplaceBottomSheetBinding.inflate(inflater, container, false)

        binding.chkRemote.isChecked = checkedState.contains("Remote")
        binding.chkOnsite.isChecked = checkedState.contains("On Site")
        binding.chkHybrid.isChecked = checkedState.contains("Hybrid")

        checkedState.clear()

        binding.topAppBar.setNavigationOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (binding.chkRemote.isChecked) checkedState.add("Remote")
        if (binding.chkOnsite.isChecked) checkedState.add("On Site")
        if (binding.chkHybrid.isChecked) checkedState.add("Hybrid")

        listener?.onValueSelected(checkedState, type!!)
    }

    fun setListener(listener: BottomSheetListener, type: BottomSheetListener.Type) {
        this.listener = listener
        this.type = type
    }

}
