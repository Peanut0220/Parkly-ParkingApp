package com.example.parkly.reservation.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.data.Interview
import com.example.parkly.data.Reservation
import com.example.parkly.databinding.ItemInterviewBinding
import com.example.parkly.databinding.ItemReservationBinding
import com.example.parkly.util.displayDate
import com.example.parkly.util.formatTime
import com.example.parkly.util.toBitmap
import io.getstream.avatarview.coil.loadImage
import kotlin.time.Duration.Companion.hours

class ReservationAdapter(
    val fn: (ViewHolder, Reservation) -> Unit = { _, _ -> },
) : ListAdapter<Reservation, ReservationAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Reservation>() {
        override fun areItemsTheSame(a: Reservation, b: Reservation) = a.id == b.id
        override fun areContentsTheSame(a: Reservation, b: Reservation) = a == b
    }

    class ViewHolder(val binding: ItemReservationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemReservationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = getItem(position)
        Log.d("TAG", "onBindViewHolder: ")
        val prev = if (position > 0) getItem(position - 1) else null

        if (reservation.status == "Pending") {
            holder.binding.lblStatus.setTextColor(Color.rgb(179, 131, 27))
        } else if (reservation.status == "Approved") {
            holder.binding.lblStatus.setTextColor(Color.GREEN)
        } else
            holder.binding.lblStatus.setTextColor(Color.RED)

        holder.binding.lblStatus.text = reservation.status

        holder.binding.lblSpace.text = reservation.spaceID


        holder.binding.lblDate.text = "${displayDate(reservation.date)}"


        holder.binding.startTime.text =
            formatTime(reservation.startTime)
        holder.binding.endTime.text = formatTime(reservation.startTime + reservation.duration)

        fn(holder, reservation)
    }

    fun formatTime(hour: Int): String {
        val suffix = if (hour >= 12) "PM" else "AM"
        val formattedHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        return "%02d:00 %s".format(formattedHour, suffix)
    }

}
