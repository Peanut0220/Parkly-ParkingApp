package com.example.parkly.job.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.data.Job
import com.example.parkly.data.SaveJob
import com.example.parkly.databinding.ItemJobCardBinding
import com.example.parkly.util.setImageBlob

class SavedJobAdapter(
    val fn: (ViewHolder, Job) -> Unit = { _, _ -> }
) : ListAdapter<SaveJob, SavedJobAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<SaveJob>() {
        override fun areItemsTheSame(a: SaveJob, b: SaveJob) = a.id == b.id
        override fun areContentsTheSame(a: SaveJob, b: SaveJob) = a == b
    }

    class ViewHolder(val binding: ItemJobCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemJobCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val savedJob = getItem(position)

        holder.binding.companyAvatar.setImageBlob(savedJob.job.company.avatar)
        holder.binding.companyName.text = savedJob.job.company.name
        holder.binding.companyLocation.text = savedJob.job.company.location
        holder.binding.jobName.text = savedJob.job.jobName
        holder.binding.lblSalary.text = "RM ${savedJob.job.minSalary} - RM ${savedJob.job.maxSalary} per month"
        holder.binding.chipJobType.text = savedJob.job.jobType
        holder.binding.chipWorkplace.text = savedJob.job.workplace
        holder.binding.chipPosition.text = savedJob.job.position
        holder.binding.timePosted.text = displayPostTime(savedJob.job.createdAt)

        fn(holder, savedJob.job)
    }

    private fun displayPostTime(postTime: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            postTime,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

}
