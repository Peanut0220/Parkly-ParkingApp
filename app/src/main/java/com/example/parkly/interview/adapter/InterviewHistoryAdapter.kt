package com.example.parkly.interview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.data.Interview
import com.example.parkly.databinding.ItemIntervieweeBinding
import com.example.parkly.util.displayDate
import com.example.parkly.util.toBitmap
import io.getstream.avatarview.coil.loadImage

class InterviewHistoryAdapter(
    val fn: (ViewHolder, Interview) -> Unit = { _, _ -> },
    val isEnterprise:Boolean
) : ListAdapter<Interview, InterviewHistoryAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Interview>() {
        override fun areItemsTheSame(a: Interview, b: Interview) = a.id == b.id
        override fun areContentsTheSame(a: Interview, b: Interview) = a == b
    }

    class ViewHolder(val binding: ItemIntervieweeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemIntervieweeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val interview = getItem(position)

        //show applicant name for enterprise, else show company name
        if (isEnterprise) {
            holder.binding.avatarView.loadImage(interview.jobApp.user.avatar.toBitmap())
            holder.binding.applicantName.text = interview.jobApp.user.name
        }
        else{
            holder.binding.avatarView.loadImage(interview.jobApp.job.company.avatar.toBitmap())
            holder.binding.applicantName.text = interview.jobApp.job.company.name
        }

        holder.binding.appliedJob.text = interview.jobApp.job.jobName
        holder.binding.lblDay.text = "Interviewed on ${displayDate(interview.date)}"
        fn(holder, interview)
    }


}
