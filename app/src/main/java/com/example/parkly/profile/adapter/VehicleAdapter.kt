package com.example.parkly.profile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.data.JobApplication
import com.example.parkly.data.Vehicle
import com.example.parkly.databinding.ItemMyAppliedJobBinding
import com.example.parkly.databinding.ItemMyVehicleBinding
import com.example.parkly.util.JobApplicationState
import com.example.parkly.util.displayPostTime
import com.example.parkly.util.toBitmap
import io.getstream.avatarview.coil.loadImage

class VehicleAdapter(
    val fn: (ViewHolder, Vehicle) -> Unit = { _, _ -> }
) : ListAdapter<Vehicle, VehicleAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Vehicle>() {
        override fun areItemsTheSame(a: Vehicle, b: Vehicle) = a.vehicleID == b.vehicleID
        override fun areContentsTheSame(a: Vehicle, b: Vehicle) = a == b
    }

    class ViewHolder(val binding: ItemMyVehicleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemMyVehicleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vehicle = getItem(position)

        holder.binding.txtVehicleNumber.text = vehicle.vehicleNumber
        holder.binding.txtVehicleDesc.text = vehicle.vehicleModel

        fn(holder, vehicle)
    }


}
