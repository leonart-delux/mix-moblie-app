package hcmute.edu.vn.noicamheo.system_setting;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import hcmute.edu.vn.noicamheo.MenuActivity;
import hcmute.edu.vn.noicamheo.R;

public class BatteryBroadcastReceiver extends BroadcastReceiver {

    private static final int LOW_BATTERY_THRESHOLD = 20;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (int) ((level / (float) scale) * 100);

            boolean isCharging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0;

            if (batteryPct <= LOW_BATTERY_THRESHOLD && !isCharging) {
                showBatterySaverNotification(context);
            }
        }
    }

    private void showBatterySaverNotification(Context context) {
        Intent actionIntent = new Intent(context, MenuActivity.class);
        actionIntent.setAction("ENABLE_BATTERY_SAVER");
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "battery_channel")
                .setSmallIcon(R.drawable.ic_battery_saver)
                .setContentTitle("Pin yếu")
                .setContentText("Bạn có muốn bật chế độ tiết kiệm pin không?")
                .addAction(R.drawable.ic_battery_saver, "Bật tiết kiệm pin", pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(2001, builder.build());
    }
}