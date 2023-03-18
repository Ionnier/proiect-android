package com.ionnier.pdma

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import timber.log.Timber


class IntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.w("Received intent")
        intent.getStringExtra("myAction")?.let {
            if (it == "notify") {
                val manager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_input_add)
                    .setContentTitle("PDMA")
                    .setContentText("Reminder to track")
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)

                val pendingIntent = NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.addFragment)
                    .createPendingIntent()
                builder.setContentIntent(pendingIntent)
                manager.notify(12345, builder.build())
            }

        }
    }
}