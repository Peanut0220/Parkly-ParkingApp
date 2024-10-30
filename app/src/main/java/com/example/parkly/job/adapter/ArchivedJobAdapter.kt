package com.example.parkly.job.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.data.Job
import com.example.parkly.databinding.ItemArchivedJobBinding
import com.example.parkly.util.setImageBlob


class ArchivedJobAdapter (
    val fn: (ViewHolder, Job) -> Unit = { _, _ -> }
): ListAdapter<Job, ArchivedJobAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(a: Job, b: Job) = a.jobID == b.jobID
        override fun areContentsTheSame(a: Job, b: Job) = a == b
    }

    class ViewHolder(val binding: ItemArchivedJobBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemArchivedJobBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val job = getItem(position)

        holder.binding.companyAvatar.setImageBlob(job.company.avatar)
        holder.binding.jobName.text = job.jobName
        holder.binding.typeWorkplacePosition.text = "${job.jobType} ・ ${job.workplace} ・ ${job.position}"
        holder.binding.salary.text = "RM ${job.minSalary} - RM ${job.maxSalary} per month"
        holder.binding.archivedDate.text = "Archived ${displayArchivedTime(job.deletedAt)}"

        fn(holder, job)
    }

    private fun displayArchivedTime(archivedTime: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            archivedTime,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

}