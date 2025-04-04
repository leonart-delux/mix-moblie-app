package hcmute.edu.vn.noicamheo;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {

    private TextView titleText, descriptionText, timeText;
    private Button closeButton;
    private Ringtone ringtone; // Dùng để phát âm thanh hệ thống

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        titleText = findViewById(R.id.title);
        descriptionText = findViewById(R.id.description);
        timeText = findViewById(R.id.timeAndData);
        closeButton = findViewById(R.id.closeButton);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        long timeInMillis = intent.getLongExtra("time", 0);

        // Kiểm tra nếu `timeInMillis` hợp lệ
        if (timeInMillis > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
            String formattedTime = sdf.format(new Date(timeInMillis));
            timeText.setText(formattedTime);
        } else {
            timeText.setText("Unknown time");
        }

        // Hiển thị dữ liệu
        titleText.setText(title);
        descriptionText.setText(description);

        // Phát âm thanh thông báo của hệ thống
        playSystemRingtone();

        // Đóng Activity và dừng âm thanh khi nhấn nút
        closeButton.setOnClickListener(v -> {
            stopSystemRingtone();
            finish();
        });
    }

    // Phát âm thanh thông báo mặc định của hệ thống
    private void playSystemRingtone() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE); // Hoặc TYPE_RINGTONE, TYPE_NOTIFICATION
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        if (ringtone != null) {
            ringtone.play();
        }
    }

    // Dừng âm thanh khi tắt báo thức
    private void stopSystemRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSystemRingtone(); // Đảm bảo âm thanh dừng khi Activity bị hủy
    }
}
