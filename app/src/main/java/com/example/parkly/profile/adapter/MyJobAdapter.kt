package com.example.parkly.profile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.data.JobApplication
import com.example.parkly.databinding.ItemMyAppliedJobBinding
import com.example.parkly.util.JobApplicationState
import com.example.parkly.util.displayPostTime
import com.example.parkly.util.toBitmap
import io.getstream.avatarview.coil.loadImage

class MyJobAdapter(
    val fn: (ViewHolder, JobApplication) -> Unit = { _, _ -> }
) : ListAdapter<JobApplication, MyJobAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<JobApplication>() {
        override fun areItemsTheSame(a: JobApplication, b: JobApplication) = a.id == b.id
        override fun areContentsTheSame(a: JobApplication, b: JobApplication) = a == b
    }

    class ViewHolder(val binding: ItemMyAppliedJobBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemMyAppliedJobBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jobApp = getItem(position)

        holder.binding.avatarView.loadImage(jobApp.job.company.avatar.toBitmap())
        holder.binding.txtJobTitle.text = jobApp.job.jobName
        holder.binding.txtCompanyName.text = jobApp.job.company.name
        holder.binding.txtLocation.text = jobApp.job.company.location
        holder.binding.txtApplyDate.text = displayPostTime(jobApp.createdAt)
        when (jobApp.status) {
            JobApplicationState.ACCEPTED.toString(),
            JobApplicationState.REJECTED.toString() -> {
                holder.binding.chip.text = jobApp.status
            }

            JobApplicationState.NEW.toString() -> {
                holder.binding.chip.visibility = View.GONE
            }

        }
        fn(holder, jobApp)
    }


}
