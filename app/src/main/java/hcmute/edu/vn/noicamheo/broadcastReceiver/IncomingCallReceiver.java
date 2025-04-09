package hcmute.edu.vn.noicamheo.broadcastReceiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.noicamheo.R;

public class IncomingCallReceiver extends BroadcastReceiver {
    private boolean hasNotified;

    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) { return; }

        String stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(stateStr) && !hasNotified) {
            hasNotified = true;
            showNotification(context);
        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(stateStr)) {
            // Reset flag when call ends
            hasNotified = false;
        }
    }

    private void showNotification(Context context) {
        String channelId = "incoming_call_channel";

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_baseline_call_received_24)
                .setContentTitle("Incoming Call")
                .setContentText("You have an incoming call!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE); // vibration, sound

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) {
            return;
        }

        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Incoming Call Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notify when incoming call is detected");
            manager.createNotificationChannel(channel);
        }

        manager.notify(1001, builder.build());
    }
}
