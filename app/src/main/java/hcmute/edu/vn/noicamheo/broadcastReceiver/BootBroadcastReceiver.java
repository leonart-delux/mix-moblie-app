package hcmute.edu.vn.noicamheo.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import hcmute.edu.vn.noicamheo.service.AlarmService;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Khởi động service khi hệ thống khởi động
            Intent serviceIntent = new Intent(context, AlarmService.class);
            context.startService(serviceIntent);
        }
    }
}
