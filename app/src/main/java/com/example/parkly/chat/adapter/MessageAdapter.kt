package com.example.parkly.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.data.ChatMessage
import com.example.parkly.databinding.ItemMyMessageBinding
import com.example.parkly.databinding.ItemOtherMessageBinding
import io.getstream.avatarview.coil.loadImage
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(
    private val userID: String,
    private val avatar: Any?,
    val fn: (RecyclerView.ViewHolder, ChatMessage) -> Unit = { _, _ -> }
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) = a.id == b.id
        override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a == b
    }


    class ViewHolderMyMessage(val binding: ItemMyMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ViewHolderOtherMessage(val binding: ItemOtherMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderID == userID) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> ViewHolderMyMessage(
                ItemMyMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> ViewHolderOtherMessage(
                ItemOtherMessageBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        val previousMessage = if (position > 0) getItem(position - 1) else null // Get the previous message

        when (holder) {
            is ViewHolderMyMessage -> {
                holder.binding.txtMyMessage.text = message.message
                holder.binding.txtMyMessageTime.text = displaySendTime(message.sendTime)
                val date = displayDate(message.sendTime, previousMessage?.sendTime)
                if (date != "") {
                    holder.binding.txtDate.visibility = View.VISIBLE
                    holder.binding.txtDate.text = date
                }
                else {
                    holder.binding.txtDate.visibility = View.GONE
                }
            }

            is ViewHolderOtherMessage -> {
                holder.binding.txtOtherMessage.text = message.message
                holder.binding.txtOtherMessageTime.text = displaySendTime(message.sendTime)
                holder.binding.avatarView.loadImage(avatar)
                val date = displayDate(message.sendTime, previousMessage?.sendTime)
                if (date != "") {
                    holder.binding.txtDate.visibility = View.VISIBLE
                    holder.binding.txtDate.text = date
                }
                else {
                    holder.binding.txtDate.visibility = View.GONE
                }
            }
        }

        fn(holder, message)
    }

    private fun displaySendTime(sendTime: Long): String {
        val sdfTimeOnly = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdfTimeOnly.format(sendTime)
    }

    private fun displayDate(sendTime: Long, previousTime: Long?): String {
        val sdfDateHuman = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return if (previousTime == null || sdfDate.format(sendTime) != sdfDate.format(previousTime)) {
            sdfDateHuman.format(sendTime)
        } else { // If the dates are the same, display nothing
            ""
        }
    }


}
