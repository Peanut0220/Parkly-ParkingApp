package com.example.parkly.home.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.data.ParkingRecord
import com.example.parkly.databinding.ItemRecordCardBinding
import com.example.parkly.util.convertToLocalMillisLegacy
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RecordAdapter(

    val fn: (ViewHolder, ParkingRecord) -> Unit = { _, _ -> }
) : ListAdapter<ParkingRecord, RecordAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<ParkingRecord>() {
        override fun areItemsTheSame(a: ParkingRecord, b: ParkingRecord) = a.recordID == b.recordID
        override fun areContentsTheSame(a: ParkingRecord, b: ParkingRecord) = a == b
    }

    class ViewHolder(val binding: ItemRecordCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemRecordCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val record = getItem(position)
        val startTimeMillis = record.startTime // Assuming this is in milliseconds
        val date = Date(startTimeMillis) // Convert to Date object
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedTime = formatter.format(date)

        // Calculate End Time
        val endTimeMillis = if (record.endTime == 0L) {
            // Use current time if endTime is not available
            convertToLocalMillisLegacy(DateTime.now().millis, "Asia/Kuala_Lumpur")
        } else {
            record.endTime
        }

        // Calculate the difference in milliseconds
        val durationMillis = endTimeMillis - startTimeMillis

        // Convert milliseconds to hours and minutes
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        val hours = totalMinutes / 60 // Full hours
        val minutes = totalMinutes % 60 // Remaining minutes

        // Define the rates
        val firstHourRate = 2.00
        val subsequentHourRate = 5.00

        // Calculate the total fee
        val totalFee = if (hours < 1 && minutes > 0) {
            firstHourRate // Charge for the first hour if it's less than 1 hour
        } else {
            firstHourRate + (hours - 1) * subsequentHourRate + if (minutes > 0) subsequentHourRate else 0.0
        }


        val formattedDate = dateFormatter.format(date)

        holder.binding.date.text = formattedDate

        // Display the data
        holder.binding.spaceID.text = record.spaceID
        holder.binding.recordID.text = "Record ID : ${record.recordID}"
        holder.binding.vehicleNum.text = record.vehicleNumber
        holder.binding.startTime.text = "Start Time - $formattedTime"
        holder.binding.date.text = formattedDate
        holder.binding.endTime.text =
            if (record.endTime == 0L) "End Time - N/A" else "End Time - ${formatter.format(Date(record.endTime))}"
        holder.binding.duration.text = "$hours hrs $minutes mins"
        holder.binding.money.text = String.format("RM%.2f", totalFee)

        // Set status text
        if (record.endTime == 0L) {
            holder.binding.status.text = "Active"
            holder.binding.status.setTextColor(Color.GREEN)

        } else {
            holder.binding.status.text = "Paid"
            holder.binding.status.setTextColor(Color.BLUE)
        }


        fn(holder, record)
    }





}
