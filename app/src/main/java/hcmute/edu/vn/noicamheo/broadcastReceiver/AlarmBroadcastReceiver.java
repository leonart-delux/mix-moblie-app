package hcmute.edu.vn.noicamheo.broadcastReceiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.noicamheo.AlarmActivity;
import hcmute.edu.vn.noicamheo.R;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        long time = intent.getLongExtra("time", 0); // Thời gian thông báo

        // Tạo Intent để mở AlarmActivity khi người dùng nhấn vào thông báo
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra("title", title);
        alarmIntent.putExtra("description", description);
        alarmIntent.putExtra("time", time);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Mở ứng dụng trong một task mới

        // Tạo PendingIntent để mở ứng dụng khi bấm vào thông báo
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Tạo Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "task_notification_channel")
                .setSmallIcon(R.drawable.ic_alarm) // Biểu tượng của thông báo
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE) // Cài đặt âm thanh và rung
                .setContentIntent(pendingIntent) // PendingIntent mở ứng dụng khi nhấn vào thông báo
                .setAutoCancel(true) // Tự động hủy khi người dùng nhấn vào
                .setCategory(NotificationCompat.CATEGORY_ALARM); // Đảm bảo thông báo có dạng cảnh báo

        // Hiển thị thông báo
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return; // Nếu NotificationManager không khả dụng, không cần làm gì
        }

        // Với Android 8.0 trở lên, cần tạo channel thông báo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("task_notification_channel", "Task Notifications", importance);
            channel.setDescription("Notifications for task reminders");
            notificationManager.createNotificationChannel(channel);
        }

        // ID của notification có thể là `time` hoặc `taskId`, đảm bảo mỗi thông báo có ID duy nhất
        notificationManager.notify((int) (time % Integer.MAX_VALUE), notificationBuilder.build());
    }
}
