package com.example.parkly.job_application.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.data.JobApplication
import com.example.parkly.databinding.ItemApplicantBinding
import com.example.parkly.util.displayPostTime
import com.example.parkly.util.toBitmap
import io.getstream.avatarview.coil.loadImage

class ApplicantAdapter(
    val fn: (ViewHolder, JobApplication) -> Unit = { _, _ -> }
) : ListAdapter<JobApplication, ApplicantAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<JobApplication>() {
        override fun areItemsTheSame(a: JobApplication, b: JobApplication) = a.id == b.id
        override fun areContentsTheSame(a: JobApplication, b: JobApplication) = a == b
    }

    class ViewHolder(val binding: ItemApplicantBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemApplicantBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jobApp = getItem(position)

        holder.binding.avatarView.loadImage(jobApp.user.avatar.toBitmap())
        holder.binding.applicantName.text = jobApp.user.name
        holder.binding.lblInfo.text = jobApp.info
        holder.binding.lblPostTime.text = displayPostTime(jobApp.createdAt)


        fn(holder, jobApp)
    }


}
