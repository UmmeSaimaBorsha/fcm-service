package com.liilab.fcmservice.fcm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.liilab.fcmservice.screen.main.MainActivity
import com.liilab.fcmservice.R
import java.io.IOException

/**
 * @author Umme Saima Borsha
 * Created on 31,August,2022
 */

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var bitmap: Bitmap? = null

    override fun onMessageReceived(p0: RemoteMessage) {
        try {
            showNotification(remoteMessage = p0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
        val imageUrl = remoteMessage.data[NotificationConstants.IMAGE_URL]

        val intent: Intent
        when(remoteMessage.notification?.clickAction)
        {
            NotificationConstants.MAIN_SCREEN_CLICK_ACTION -> {
                intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            else -> {
                intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }

        if (TextUtils.isEmpty(imageUrl) || imageUrl == null) {
            if (title != null && body != null) showNotificationMessage(
                title = title,
                message = body,
                intent = intent
            )
        } else {
            if (title != null && body != null) showNotificationMessageWithBigImage(
                title = title,
                message = body,
                intent = intent,
                imageUrl = imageUrl
            )
        }
    }

    /**
     * Showing notification with text and image
     */
    private fun showNotificationMessageWithBigImage(
        title: String,
        message: String,
        intent: Intent,
        imageUrl: String
    ) {
        showNotificationMessage(
            title = title,
            message = message,
            intent = intent,
            imageUrl = imageUrl
        )
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun showNotificationMessage(
        title: String,
        message: String,
        intent: Intent,
        imageUrl: String? = null
    ) {
        if (TextUtils.isEmpty(message)) return

        val resultPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
        val mBuilder = NotificationCompat.Builder(this, this.packageName)
        val alarmSound =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/raw/notification")

        if (!TextUtils.isEmpty((imageUrl))) {
            imageUrl?.let { getBitmapFromURL(it) }
            Handler(Looper.getMainLooper()).postDelayed({
                if (bitmap != null) {
                    showBigNotification(
                        bitmap!!,
                        mBuilder,
                        title,
                        message,
                        resultPendingIntent,
                        alarmSound
                    )
                } else {
                    showSmallNotification(mBuilder, title, message, resultPendingIntent, alarmSound)
                }
                playNotificationSound()
            }, 1000)


        } else {
            showSmallNotification(mBuilder, title, message, resultPendingIntent, alarmSound)
            playNotificationSound()
        }

    }

    private fun playNotificationSound() {
        try {
            val alarmSound =
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/raw/notification")
            val r = RingtoneManager.getRingtone(applicationContext, alarmSound)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showBigNotification(
        bitmap: Bitmap,
        mBuilder: NotificationCompat.Builder,
        title: String,
        message: String,
        resultPendingIntent: PendingIntent,
        alarmSound: Uri
    ) {
        val bigPictureStyle = NotificationCompat.BigPictureStyle()
        bigPictureStyle.bigPicture(bitmap)
        bigPictureStyle.bigLargeIcon(null)
        val notificationIcon = R.drawable.ic_launcher_background

        val notification: Notification = mBuilder
            .setTicker(title)
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentIntent(resultPendingIntent)
            .setSound(alarmSound)
            .setSmallIcon(notificationIcon)
            .setLargeIcon(bitmap)
            .setStyle(bigPictureStyle)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    applicationContext.resources,
                    notificationIcon
                )
            )
            .setContentText(message)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                this.packageName,
                this.packageName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(NotificationConstants.NOTIFICATION_ID_BIG_IMAGE, notification)
    }

    private fun showSmallNotification(
        mBuilder: NotificationCompat.Builder,
        title: String,
        message: String,
        resultPendingIntent: PendingIntent,
        alarmSound: Uri
    ) {

        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.addLine(message)

        val notification: Notification = mBuilder
            .setTicker(title)
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentIntent(resultPendingIntent)
            .setSound(alarmSound)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentText(message)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                this.packageName, this.packageName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(NotificationConstants.NOTIFICATION_ID, notification)
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    private fun getBitmapFromURL(strURL: String) {
        try {
            Glide
                .with(this)
                .setDefaultRequestOptions(
                    RequestOptions()
                        .timeout(30000)
                )
                .asBitmap()
                .load(strURL)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        bitmap = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}

                })

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("LongLogTag")
    override fun onNewToken(token: String) {
        /**
         * If you want to send messages to applicationContext application instance or
         * manage applicationContext apps subscriptions on the server side, send the
         * Instance ID token to your app server.
         */

        /**
         *
         * The registration token may change when:
         *         The app is restored on a new device
         *         The user uninstalls/reinstall the app
         *         The user clears app data.
         */

        Log.d(TAG, "onNewToken: $token")

    }

    companion object {
        private const val TAG = "MyFirebaseMessagingService"
    }
}