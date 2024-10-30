package com.example.parkly.chat.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.chat.adapter.ChatAdapter
import com.example.parkly.data.Chat
import com.example.parkly.data.ChatMessage
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentChatBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class ChatFragment : Fragment() {

    companion object {
        fun newInstance() = ChatFragment()
    }

    private val nav by lazy { findNavController() }
    private lateinit var binding: FragmentChatBinding
    private lateinit var adapter: ChatAdapter
    private val userVM: UserViewModel by activityViewModels()
    private var chatList = mutableListOf<Chat>()
    private var lastSeen = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        chatList.clear()

        adapter = ChatAdapter { holder, chat ->
            holder.binding.chat.setOnClickListener { message(chat.id) }
            holder.binding.avatarView.setOnClickListener { profile(chat.receiverId) }
        }
        binding.rvChat.adapter = adapter
        displayChatList(userVM.getAuth().uid)
        return binding.root
    }

    fun displayChatList(userID: String) {
        val chatRoomsRef = FirebaseDatabase.getInstance().getReference("chatRooms")

        chatRoomsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val chatRoomId = dataSnapshot.key
                if (chatRoomId != null && chatRoomId.contains(userID)) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val userIDs = chatRoomId.split("_")
                        val receiver = withContext(Dispatchers.IO) {
                            if (userIDs[0] == userID) userVM.get(userIDs[1]) else userVM.get(userIDs[0])
                        }
                        val chat = Chat(
                            id = chatRoomId,
                            receiverId = receiver!!.uid,
                            receiverName = receiver.name,
                            avatar = receiver.avatar,
                            latestMessage = getLatestMessage(chatRoomId),
                            numOfUnreadMsg = calculateUnreadMessages(chatRoomId, userID),
                            isReceiverOnline = onlineStatusListener(receiver.uid, chatRoomId)
                        )
                        latestMessageListener(chatRoomId)
                        withContext(Dispatchers.Main) {
                            chatList.add(chat)
                            adapter.submitList(chatList.sortedByDescending { it.latestMessage.sendTime })
                        }
                    }
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }

    private val messageListeners = mutableSetOf<String>()

    suspend fun latestMessageListener(chatRoomId: String) {
        if (messageListeners.contains(chatRoomId)) return
        messageListeners.add(chatRoomId)
        withContext(Dispatchers.IO) {
            val database = FirebaseDatabase.getInstance()
            val messageRef = database.getReference("chatRooms").child(chatRoomId).child("messages")
            var latestMessage = ChatMessage()

            val latestMessageQuery = messageRef.orderByChild("sendTime").limitToLast(1)

            latestMessageQuery.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(ChatMessage::class.java)
                    message?.let {
                        latestMessage = it
                        // REFRESH THE CHAT LIST HERE
                        Log.d("LOOP", latestMessage.message)
                        val updateChat = chatList.find { it.id == chatRoomId }
                        updateChat?.latestMessage = latestMessage

                        CoroutineScope(Dispatchers.Main).launch {
                            updateChat?.numOfUnreadMsg =
                                calculateUnreadMessages(chatRoomId, userVM.getAuth().uid)
                            adapter.notifyDataSetChanged()
                            adapter.submitList(chatList.sortedByDescending { it.latestMessage.sendTime })
                        }

                    }
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                }

                override fun onCancelled(p0: DatabaseError) {
                }

            })
        }
    }

    fun onlineStatusListener(userId: String, chatRoomId: String): Boolean {
        var isReceiverOnline = false

        val onlineStatusRef =
            FirebaseDatabase.getInstance().getReference("onlineStatus").child(userId)
        onlineStatusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val isOnline = dataSnapshot.getValue(Boolean::class.java)
                isOnline?.let {

                    if (isOnline) {
                        isReceiverOnline = true
                    }

                    if (chatList.isNotEmpty()) {
                        val updateChat = chatList.find { it.id == chatRoomId }
                        updateChat?.isReceiverOnline = isOnline
                        adapter.notifyDataSetChanged()
                        adapter.submitList(chatList.sortedByDescending { it.latestMessage.sendTime })
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        return isReceiverOnline
    }


    suspend fun getLatestMessage(chatRoomId: String): ChatMessage {
        return withContext(Dispatchers.IO) {
            var latestMessage = ChatMessage()

            val messageRef =
                FirebaseDatabase.getInstance().getReference("chatRooms/$chatRoomId/messages")

            val dataSnapshot = withContext(Dispatchers.Default) {
                messageRef.orderByChild("sendTime").limitToLast(1).get().await()
            }

            dataSnapshot.children.forEach {
                latestMessage = ChatMessage(
                    id = it.child("id").getValue(String::class.java)!!,
                    message = it.child("message").getValue(String::class.java)!!,
                    sendTime = it.child("sendTime").getValue(Long::class.java)!!,
                    senderID = it.child("senderID").getValue(String::class.java)!!
                )
            }

            latestMessage
        }

    }

    suspend fun calculateUnreadMessages(chatRoomId: String, userId: String): Int {
        return withContext(Dispatchers.IO) {
            var unreadMessage = 0

            val database = FirebaseDatabase.getInstance()
            val messageRef = database.getReference("chatRooms/$chatRoomId/messages")
            val lastSeenRef = database.getReference("chatRooms/$chatRoomId/lastSeen/$userId")

            lastSeen = withContext(Dispatchers.Default) {
                lastSeenRef.get().await().getValue(Long::class.java) ?: 0L
            }

            val dataSnapshot = withContext(Dispatchers.Default) {
                messageRef.orderByChild("sendTime").startAt(lastSeen.toDouble()).get().await()
            }

            unreadMessage = dataSnapshot.children.count()

            unreadMessage
        }

    }

    private fun message(chatRoomId: String) {
        nav.navigate(
            R.id.chatTextFragment, bundleOf(
                "chatRoomId" to chatRoomId
            )
        )
    }

    private fun profile(userID: String) {
        nav.navigate(
            R.id.userProfileFragment, bundleOf(
                "userID" to userID
            )
        )
    }

}