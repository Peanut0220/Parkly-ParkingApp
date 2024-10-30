package com.example.parkly.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.R
import com.example.parkly.data.Chat
import com.example.parkly.databinding.ItemChatBinding
import com.example.parkly.util.toBitmap
import io.getstream.avatarview.coil.loadImage
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(
    val fn: (ViewHolder, Chat) -> Unit = { _, _ -> }
) : ListAdapter<Chat, ChatAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(a: Chat, b: Chat) = a.id == b.id
        override fun areContentsTheSame(a: Chat, b: Chat) = a == b
    }

    class ViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = getItem(position)

//      holder.binding.companyAvatar.setImageBlob(job.company.avatar)
        holder.binding.chatName.text = chat.receiverName
        holder.binding.chatContent.text = chat.latestMessage.message
        holder.binding.chatTime.text = displaySendTime(chat.latestMessage.sendTime)
        val avatar =
            if (chat.avatar.toBytes().isEmpty())
                R.drawable.round_account_circle_24
            else
                chat.avatar.toBitmap()
        holder.binding.avatarView.loadImage(avatar)
        with(holder.binding.chatMessagesNumber) {
            visibility = if (chat.numOfUnreadMsg > 0) View.VISIBLE else View.GONE
            text = if (chat.numOfUnreadMsg > 9) "9+" else chat.numOfUnreadMsg.toString()
        }
        holder.binding.avatarView.indicatorEnabled = chat.isReceiverOnline
        fn(holder, chat)
    }

    private fun displaySendTime(sendTime: Long): String {

        val sdf =
            if (sendTime < DateTime.now().withTimeAtStartOfDay().millis)
                SimpleDateFormat("M/d/yyyy", Locale.getDefault())
            else
                SimpleDateFormat("hh:mm a", Locale.getDefault())

        return sdf.format(sendTime)
    }

}
