package com.example.parkly.util

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.graphics.scale
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.example.parkly.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.Blob
import com.google.firebase.messaging.FirebaseMessaging
import io.getstream.avatarview.AvatarView
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SERVER_KEY =
    "11fdde5e45bfdbc45b6fb11c82d2f3d394a327"

fun sendPushNotification(title: String, message: String, receiverToken: String) {
    val jsonObject = JSONObject()

    val notificationObj = JSONObject()
    notificationObj.put("title", title)
    notificationObj.put("body", message)

    // For future used
    //val dataObj = JSONObject()
    //dataObj.put("fragment", fragment)

    jsonObject.put("notification", notificationObj)
    //jsonObject.put("data", dataObj)
    jsonObject.put("to", receiverToken)

    callApi(jsonObject)
}

//create chat room
suspend fun isChatRoomExist(chatRoomId: String): Boolean {

    val chatRoomsRef = FirebaseDatabase.getInstance("https://advanceparkingapp-3ec88-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("chatRooms")
    // Await directly here without runBlocking
    val dataSnapshot = chatRoomsRef.get().await()
    var isExist = false
    dataSnapshot.children.forEach {
        val thisChatRoomId = it.key.toString()

        if (thisChatRoomId == chatRoomId) {
            isExist = true

            return@forEach
        }
    }
    return isExist
}

fun message(chatRoomId: String, nav: NavController) {
    nav.navigate(
        R.id.chatTextFragment, bundleOf(
            "chatRoomId" to chatRoomId
        )
    )
}


fun createChatroom(chatRoomId: String) {
    val chatRoomsRef = FirebaseDatabase.getInstance().getReference("chatRooms")
    chatRoomsRef.child(chatRoomId).get().addOnSuccessListener { snapshot ->
        if (!snapshot.exists()) {
            //potential problem: duplicate chat room
            chatRoomsRef.child(chatRoomId).setValue(true)
        }
    }
}

fun callApi(jsonObject: JSONObject) {
    val JSON: MediaType = "application/json".toMediaType()
    val client = OkHttpClient()
    val url = "https://fcm.googleapis.com/v1/projects/myproject-b5ae1/messages:send"
    val body = RequestBody.create(JSON, jsonObject.toString())
    val request = Request.Builder()
        .url(url)
        .post(body)
        .header(
            "Authorization",
            "Bearer $SERVER_KEY"
        )
        .build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("ERROR", e.toString())
        }

        override fun onResponse(call: Call, response: Response) {
            Log.e("SUCCESS", response.toString())
        }
    })
}

fun getToken(): MutableLiveData<String> {
    val tokenLive = MutableLiveData<String>()
    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
        if (!task.isSuccessful) {
            Log.w("TOKEN", "Fetching FCM registration token failed", task.exception)
            return@OnCompleteListener
        }

        // Get new FCM registration token
        val token = task.result
        tokenLive.value = token ?: ""
    })
    return tokenLive
}

fun Fragment.toast(text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun Fragment.snackbar(text: String) {
    Snackbar.make(view!!, text, Snackbar.LENGTH_SHORT).show()
}

fun View.snackbar(text: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, text, duration).show()
}

fun View.dialog(
    title: String,
    msg: String,
    txtNegative: String? = "No",
    txtPositive: String? = "Yes",
    onNegativeClick: ((DialogInterface, Int) -> Unit)? = null,
    onPositiveClick: ((DialogInterface, Int) -> Unit)? = null
) {
    val builder = MaterialAlertDialogBuilder(context!!)
        .setTitle(title)
        .setMessage(msg)
    if (onNegativeClick != null) {
        builder.setNegativeButton(txtNegative) { dialog, which ->
            onNegativeClick(dialog, which)
        }
    } else {
        builder.setNegativeButton(txtNegative, null)
    }

    if (onPositiveClick != null) {
        builder.setPositiveButton(txtPositive) { dialog, which ->
            onPositiveClick(dialog, which)
        }
    } else {
        builder.setPositiveButton(txtPositive, null)
    }

    builder.show()
}

fun Fragment.dialog(
    title: String,
    msg: String,
    txtNegative: String? = "No",
    txtPositive: String? = "Yes",
    onNegativeClick: ((DialogInterface, Int) -> Unit)? = null,
    onPositiveClick: ((DialogInterface, Int) -> Unit)? = null
) {
    val builder = MaterialAlertDialogBuilder(context!!)
        .setTitle(title)
        .setMessage(msg)
    if (onNegativeClick != null) {
        builder.setNegativeButton(txtNegative) { dialog, which ->
            onNegativeClick(dialog, which)
        }
    } else {
        builder.setNegativeButton(txtNegative, null)
    }

    if (onPositiveClick != null) {
        builder.setPositiveButton(txtPositive) { dialog, which ->
            onPositiveClick(dialog, which)
        }
    } else {
        builder.setPositiveButton(txtPositive, null)
    }

    builder.show()
}

fun Fragment.loadingDialog(): Dialog {
    val dialog = Dialog(requireContext())
    dialog.setContentView(R.layout.layout_loading)
    return dialog
}

fun Fragment.dialogProfileNotComplete(nav: NavController) {
    val dialog = AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.complete_now))
        .setMessage(getString(R.string.dialog_complete_your_profile))
        .setPositiveButton(getString(R.string.complete_now)) { _, _ ->
            nav.navigate(R.id.profileUpdateFragment)
        }
        .create()

    // Make the dialog non-cancelable
    dialog.setCancelable(false)
    dialog.setCanceledOnTouchOutside(false)
    dialog.show()
}

fun Fragment.displayErrorHelper(view: TextInputLayout, errorMsg: String) {
    view.requestFocus()
    view.error = errorMsg
    view.errorIconDrawable = null
}

fun Fragment.hideErrorHelper(view: TextInputLayout) {
    view.error = ""
}

fun Context.intentWithoutBackstack(
    context: Context,
    targetClass: Class<*>,
    extras: Bundle? = null
) {
    val intent = Intent(context, targetClass)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    extras?.let {
        intent.putExtras(it)
    }
    context.startActivity(intent)
}

fun displayPostTime(postTime: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        postTime,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
}

fun View.disable() {
    isEnabled = false
    isClickable = false
}

fun displayDate(postTime: Long): String {
    val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return format.format(Date(postTime))
}


fun combineDateTime(date: Long, hour: Int, minute: Int): DateTime {
    val dateTime = DateTime(date)
    val time = LocalTime(hour, minute)
    return dateTime.withTime(time)
}

fun formatTime(hour: Int, minute: Int): String {
    val dateTime = DateTime.now().withTime(hour, minute, 0, 0)
    val formatter = DateTimeFormat.forPattern("hh:mm a")
    return dateTime.toString(formatter)
}


fun Fragment.showFileSize(l: Long): String {
    var size = l / 1024.0
    var unit = "KB"
    if (size > 1024) {
        size /= 1024.0
        unit = "MB"
    }
    return String.format("%.2f %s", size, unit)
}
// ----------------------------------------------------------------------------
// Bitmap Extensions
// ----------------------------------------------------------------------------

// Usage: Crop and resize bitmap (upscale)
fun Bitmap.crop(width: Int, height: Int): Bitmap {
    // Source width, height and ratio
    val sw = this.width
    val sh = this.height
    val sratio = 1.0 * sw / sh

    // Target offset (x, y), width, height and ratio
    val x: Int
    val y: Int
    val w: Int
    val h: Int
    val ratio = 1.0 * width / height

    if (ratio >= sratio) {
        // Retain width, calculate height
        w = sw
        h = (sw / ratio).toInt()
        x = 0
        y = (sh - h) / 2
    } else {
        // Retain height, calculate width
        w = (sh * ratio).toInt()
        h = sh
        x = (sw - w) / 2
        y = 0
    }

    return Bitmap
        .createBitmap(this, x, y, w, h) // Crop
        .scale(width, height) // Resize
}

// Usage: Convert from Bitmap to Firebase Blob
fun Bitmap.toBlob(): Blob {
    ByteArrayOutputStream().use {
        compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, it)
        return Blob.fromBytes(it.toByteArray())
    }
}

// ----------------------------------------------------------------------------
// Firebase Blob Extensions
// ----------------------------------------------------------------------------

// Usage: Convert from Blob to Bitmap
fun Blob.toBitmap(): Bitmap? {
    val bytes = toBytes()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

// ----------------------------------------------------------------------------
// ImageView Extensions
// ----------------------------------------------------------------------------

// Usage: Crop to Firebase Blob
fun AvatarView.cropToBlob(width: Int, height: Int): Blob {
    return drawable?.toBitmapOrNull()?.crop(width, height)?.toBlob() ?: Blob.fromBytes(ByteArray(0))
}

fun ImageView.cropToBlob(width: Int, height: Int): Blob {
    return drawable?.toBitmapOrNull()?.crop(width, height)?.toBlob() ?: Blob.fromBytes(ByteArray(0))
}

// Usage: Load Firebase Blob
fun ImageView.setImageBlob(blob: Blob) {
    setImageBitmap(blob.toBitmap())
}

fun isBlobEmpty(blob: Blob): Boolean {
    return blob.toBytes().isEmpty()
}

//future improvement used
/*
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Launchpad"
        val descriptionText = "Launchpad Notification"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun sendNotificationWithIntent(context: Context, title: String, message: String, fragment: String, bundle: String) {
    val intent = Intent(context, UserActivity::class.java)
    intent.putExtra(fragment,bundle)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    val builder = NotificationCompat.Builder(context, "CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@with
        }
        notify(Random.nextInt(10000, 100000), builder.build())
    }
}
*/
