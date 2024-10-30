package com.example.parkly.chat.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.chat.adapter.MessageAdapter
import com.example.parkly.data.ChatMessage
import com.example.parkly.data.User
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentChatTextBinding
import com.example.parkly.util.sendPushNotification
import com.example.parkly.util.toBitmap
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ChatTextFragment : Fragment() {

    private val nav by lazy { findNavController() }
    private val userVM: UserViewModel by activityViewModels()
    private lateinit var adapter: MessageAdapter
    private var msgList = mutableListOf<ChatMessage>()

    private val chatRoomId by lazy { arguments?.getString("chatRoomId") ?: "" }
    private lateinit var currentUser: User
    private lateinit var otherUser: User

    companion object {
        fun newInstance() = ChatTextFragment()
    }

    private lateinit var binding: FragmentChatTextBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatTextBinding.inflate(inflater, container, false)

        val userIDs = chatRoomId.split("_")

        //TO SOLVE DISPLAY NAME ISSUE
        currentUser = userVM.get(userVM.getAuth().uid)!!

        otherUser =
            if (userIDs[0] == userVM.getAuth().uid) {
                userVM.get(userIDs[1])!!
            } else {
                userVM.get(userIDs[0])!!
            }

        binding.topAppBar.title = otherUser.name
        val avatar =
            if (otherUser.avatar.toBytes().isEmpty())
                R.drawable.round_account_circle_24
            else
                otherUser.avatar.toBitmap()

        adapter = MessageAdapter(currentUser.uid, avatar)
        binding.rvMessages.adapter = adapter
        adapter.submitList(msgList)
        displayMessage(chatRoomId)

        binding.btnSend.setOnClickListener { sendMessage() }

        binding.topAppBar.setOnClickListener {
            nav.navigateUp()
        }

        onlineStatusListener(otherUser.uid)
        updateLastSeenTime(chatRoomId, currentUser.uid)

        return binding.root
    }


    fun sendMessage() {
        val messageText = binding.edtMessage.text.toString()
        if (messageText.isEmpty()) return

        val database = FirebaseDatabase.getInstance()
        val messageRef = database.getReference("chatRooms").child(chatRoomId).child("messages")
        val messageId = messageRef.push().key ?: return
        val message = ChatMessage(
            id = messageId,
            senderID = currentUser.uid,
            message = messageText,
            sendTime = DateTime.now().millis
        )

        messageRef.child(messageId).setValue(message).addOnSuccessListener {
            binding.edtMessage.text?.clear()
            sendPushNotification(currentUser.name, messageText, otherUser.token)
        }

    }

    fun displayMessage(chatRoomId: String) {
        val database = FirebaseDatabase.getInstance()
        val messageRef = database.getReference("chatRooms").child(chatRoomId).child("messages")

        messageRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ChatMessage::class.java)
                message?.let {
                    msgList.add(message)
                    binding.rvMessages.scrollToPosition(msgList.size - 1)
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

    fun updateLastSeenTime(chatRoomId: String, userId: String) {
        val lastSeenRef = FirebaseDatabase.getInstance().getReference("chatRooms")
            .child(chatRoomId).child("lastSeen").child(userId)

        Log.d("LASTSEENUSERID", userId)

        lastSeenRef.setValue(ServerValue.TIMESTAMP)
    }

    suspend fun getLastSeenTime(chatRoomId: String, userId: String): Long {

        return suspendCoroutine { cont ->
            val lastSeenRef = FirebaseDatabase.getInstance().getReference("chatRooms")
                .child(chatRoomId).child("lastSeen").child(userId)
            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val lastSeenTime = dataSnapshot.getValue(Long::class.java)
                    Log.d("LASTSEENTIME", lastSeenTime.toString())
                    cont.resume(lastSeenTime ?: 0L)
                    lastSeenRef.removeEventListener(this) // Remove the listener


                }

                override fun onCancelled(p0: DatabaseError) {
                    cont.resume(0L)
                }
            }
            lastSeenRef.addValueEventListener(listener)
        }

    }

    fun onlineStatusListener(userId: String) {
        val onlineStatusRef =
            FirebaseDatabase.getInstance().getReference("onlineStatus").child(userId)
        onlineStatusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val isOnline = dataSnapshot.getValue(Boolean::class.java)
                isOnline?.let {
                    if (isOnline) {
                        binding.topAppBar.subtitle = "Online"
                    } else {
                        lifecycleScope.launch {
                            binding.topAppBar.subtitle =
                                displayLastSeenTime(getLastSeenTime(chatRoomId, otherUser.uid))
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

    }

    fun displayLastSeenTime(lastSeenTime: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        return if (lastSeenTime == 0L) "" else "Last Seen: " + sdf.format(lastSeenTime)
    }

    override fun onPause() {
        super.onPause()
        updateLastSeenTime(chatRoomId, currentUser.uid)
    }

    override fun onResume() {
        super.onResume()
        updateLastSeenTime(chatRoomId, currentUser.uid)
    }

}