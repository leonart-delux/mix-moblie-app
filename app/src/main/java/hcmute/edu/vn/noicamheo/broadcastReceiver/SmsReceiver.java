package hcmute.edu.vn.noicamheo.broadcastReceiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.noicamheo.ChatDetailsActivity;
import hcmute.edu.vn.noicamheo.R;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private static final String CHANNEL_ID = "SMS_Notification_Channel";
    private static final String CHANNEL_NAME = "SMS Notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            Log.d(TAG, "SMS received");

            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            String format = intent.getStringExtra("format");

            if (pdus == null) return;

            Map<String, StringBuilder> messagesByAddress = new HashMap<>();

            // Xử lý tin nhắn
            for (Object pdu : pdus) {
                SmsMessage smsMessage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? SmsMessage.createFromPdu((byte[]) pdu, format)
                        : SmsMessage.createFromPdu((byte[]) pdu);

                if (smsMessage == null) continue;

                String sender = smsMessage.getDisplayOriginatingAddress();
                String content = smsMessage.getMessageBody();

                messagesByAddress.computeIfAbsent(sender, k -> new StringBuilder()).append(content);
            }

            // Hiển thị thông báo
            for (Map.Entry<String, StringBuilder> entry : messagesByAddress.entrySet()) {
                String sender = entry.getKey();
                String fullMessage = entry.getValue().toString();
                String senderName = getContactName(context, sender);

                showSmsNotification(context, senderName, sender, fullMessage);
            }
        }
    }

    private void showSmsNotification(Context context, String senderName, String senderId, String content) {
        createNotificationChannel(context);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = ChatDetailsActivity.newIntent(context, senderName, senderId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(senderName)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for SMS notifications");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private String getContactName(Context context, String phoneNumber) {
        // Logic lấy tên từ danh bạ nếu cần, hiện tại trả về số điện thoại
        return phoneNumber;
    }
}