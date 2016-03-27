package com.utdallas.hpt150030.morsetalktry;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by Hardik on 3/26/2016.
 */
public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = "MyGcmListenerService";
    private long[] vibratePattern;
    private Vibrator vibrator;
    private DBOperations dbOperations;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);


        String messageParts[] = message.split(":");
        String from = messageParts[0];
        String messagePattern = messageParts[1];

        vibratePattern = new long[(messagePattern.length() * 2) + 1];
        int count = 0;
        vibratePattern[count++] = 0;
        //Toast.makeText(MorseActivity.this, strBuilder.toString(), Toast.LENGTH_SHORT).show();
        for (char c : messagePattern.toCharArray()) {
            switch (c) {
                case 'd':
                    vibratePattern[count++] = 225;
                    vibratePattern[count++] = 225;
                    break;
                case 'u':
                    vibratePattern[count++] = 675;
                    vibratePattern[count++] = 225;
                    break;
                case 'c':
                    vibratePattern[count++] = 0;
                    vibratePattern[count++] = 675;
                    break;
                case 'w':
                    vibratePattern[count++] = 0;
                    vibratePattern[count++] = 1575;
                    break;
            }

        }


        vibrator.vibrate(vibratePattern, -1);

        dbOperations = new DBOperations(this);
        Message msg = new Message();
        msg.setUser(from);
        msg.setMessage(messagePattern);
        msg.setSentBy("N");
        msg.setTime(new java.sql.Timestamp(new java.util.Date().getTime()).toString());
        dbOperations.insertMessage(msg);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_cast_dark)
                .setContentTitle("MorseTalk Message")
                .setContentText("From:"+from)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
