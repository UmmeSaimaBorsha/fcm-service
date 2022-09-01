package com.liilab.fcmservice.screen.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.liilab.fcmservice.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d(TAG, "Fetching FCM registration token failed: ${task.exception}")
                    return@OnCompleteListener
                }
                val token = task.result
                Log.d(TAG, "Firebase Push Token: $token")
            })
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}