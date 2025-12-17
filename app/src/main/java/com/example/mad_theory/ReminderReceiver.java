package com.example.mad_theory;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "study_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        createChannel(context);

        Intent openIntent = new Intent(context, ReminderActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Use a proper notification icon (white icon on transparent background)
        // Notification icons must be white/transparent, not colored
        int iconRes = android.R.drawable.ic_dialog_info; // Fallback icon
        
        // Try to use the notifications icon, but fallback if not available
        try {
            iconRes = context.getResources().getIdentifier("ic_notifications", "drawable", context.getPackageName());
            if (iconRes == 0) {
                iconRes = android.R.drawable.ic_dialog_info;
            }
        } catch (Exception e) {
            iconRes = android.R.drawable.ic_dialog_info;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconRes)
                .setContentTitle(title != null ? title : "Study Reminder")
                .setContentText(message != null ? message : "It's time to study!")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, lights
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message != null ? message : "It's time to study!"));

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Study Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }
    }
}


