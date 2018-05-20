package com.andima.gestordeapps;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Andima on 21/04/2018.
 */

public class ServicioMensajeria extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("mensajeFCM", "From: " + remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("mensajeFCM", "Message data payload: " + remoteMessage.getData());
        }
            // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("mensajeFCM", "Message Notification Body: " +
                    remoteMessage.getNotification().getBody());

            Intent intent = new Intent(remoteMessage.getNotification().getClickAction());
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager nm = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
            NotificationCompat.Builder nb = new NotificationCompat.Builder(getApplicationContext(), "1")
                    .setSmallIcon(android.R.drawable.ic_menu_info_details)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setVibrate(new long[]{100})
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel nc = new NotificationChannel("2",
                        getString(R.string.nombre_canal2), NotificationManager.IMPORTANCE_DEFAULT);
                // Configurar el canal de comunicación
                if (nm != null) {
                    nm.createNotificationChannel(nc);
                }
            }
            if (nm != null) {
                nm.notify(1, nb.build());
            }
        }
    }
}
