package hcmute.edu.vn.noicamheo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SmsBackgroundService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SmsBackgroundService", "Service started");
        // Không cần Handler nữa, chỉ cần đảm bảo service được gọi lại khi cần
        return START_STICKY; // Service sẽ được khởi động lại nếu bị hệ thống kill
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("SmsBackgroundService", "Task removed, scheduling restart");
        // Lên lịch khởi động lại service bằng WorkManager hoặc Intent
        Intent restartIntent = new Intent(this, SmsBackgroundService.class);
        startService(restartIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.d("SmsBackgroundService", "Service destroyed");
        super.onDestroy();
    }
}