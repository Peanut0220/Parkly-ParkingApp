package com.example.parkly.job.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.data.Job
import com.example.parkly.databinding.ItemJobCardBinding
import com.example.parkly.util.setImageBlob

class JobAdapter(
    val fn: (ViewHolder, Job) -> Unit = { _, _ -> }
    ) : ListAdapter<Job, JobAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(a: Job, b: Job) = a.jobID == b.jobID
        override fun areContentsTheSame(a: Job, b: Job) = a == b
    }

    class ViewHolder(val binding: ItemJobCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemJobCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val job = getItem(position)



        fn(holder, job)
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
