package hcmute.edu.vn.noicamheo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.noicamheo.AlarmActivity;
import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.broadcastReceiver.AlarmBroadcastReceiver;
import hcmute.edu.vn.noicamheo.database.DatabaseHelper;
import hcmute.edu.vn.noicamheo.entity.Task;

public class AlarmService extends Service {
    private DatabaseHelper databaseHelper;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        databaseHelper = new DatabaseHelper(this);

        // Kiểm tra và lấy các task đã lên lịch, gửi thông báo nếu cần
        triggerAlarmNotifications();

        return START_STICKY;
    }

    private void triggerAlarmNotifications() {
        // Lấy tất cả các task đã lên lịch từ cơ sở dữ liệu
        List<Task> tasks = databaseHelper.getAllTasks();

        // Duyệt qua các task để kiểm tra thời gian thông báo
        for (Task task : tasks) {
            if (shouldTriggerNotification(task)) {
                // Gửi thông báo tới AlarmBroadcastReceiver
                Intent alarmIntent = new Intent(this, AlarmBroadcastReceiver.class);
                alarmIntent.putExtra("title", task.getTitle());
                alarmIntent.putExtra("description", task.getDescription());
                alarmIntent.putExtra("time", task.getTime());  // Dữ liệu time từ Task

                // Gửi broadcast để AlarmBroadcastReceiver xử lý
                sendBroadcast(alarmIntent);
            }
        }
    }

    private boolean shouldTriggerNotification(Task task) {
        String taskTimeStr = task.getTime(); // "HH:mm"
        String taskDateStr = task.getDate(); // "dd/MM/yyyy"

        try {
            // Gộp cả ngày và giờ để so sánh
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date taskDateTime = sdf.parse(taskDateStr + " " + taskTimeStr);

            long currentTimeMillis = System.currentTimeMillis();
            long taskTimeMillis = taskDateTime.getTime();

            // Trigger nếu thời gian task <= thời gian hiện tại
            return currentTimeMillis >= taskTimeMillis;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
