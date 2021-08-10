package com.main.notificationapp.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleService
import com.main.notificationapp.R
import com.main.notificationapp.ui.activities.MainActivity
import com.main.notificationapp.ui.viewmodels.MainViewModel
import com.main.notificationapp.utils.Constants
import com.main.notificationapp.utils.Constants.FROM_SERVICE
import com.main.notificationapp.utils.Constants.SENDING_ARTICLE

const val NOTIFICATION_NO = 1

class Services : LifecycleService(){

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        when(intent?.action){
            Constants.FIRST_SERVICE_RUN -> {
                createNotification()
            }
            Constants.UPDATED->{
                val bundle = intent.extras
                performUpdates((bundle?.get("title") as String), bundle["url"] as String, bundle["content"] as String)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = FROM_SERVICE
        }

        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = createBaseNotification()
            .setContentIntent(pendingIntent)
            .build()
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_NO, notification)
    }

    private fun performUpdates(title: String, url: String, content: String){
        val intent = Intent(this, MainActivity::class.java).apply {
            action = SENDING_ARTICLE
        }
        intent.putExtras(bundleOf("title" to title, "url" to url))

        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = createBaseNotification()
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentText(content)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .build()
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_NO, notification)
    }

    private fun createBaseNotification() : NotificationCompat.Builder{

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Breaking News")
            .setContentText("Welcome to your Exclusive News App")
            .setSubText("Read, Save and Search On Your Favourite Subjects")
            .setSmallIcon(R.drawable.ic_news)
            .setPriority(NotificationCompat.PRIORITY_LOW)

    }
    
    private fun createChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                lightColor = Color.RED
                enableLights(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

}