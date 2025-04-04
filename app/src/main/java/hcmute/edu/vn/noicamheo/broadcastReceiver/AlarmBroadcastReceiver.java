package hcmute.edu.vn.noicamheo.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import hcmute.edu.vn.noicamheo.AlarmActivity;


public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        long time = intent.getLongExtra("time", 0);

        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra("title", title);
        alarmIntent.putExtra("description", description);
        alarmIntent.putExtra("time", time);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(alarmIntent);
    }
}
