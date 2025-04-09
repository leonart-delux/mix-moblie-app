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

        // Đóng Activity khi nhấn nút
        closeButton.setOnClickListener(v -> {
            finish();
        });
    }

    // Phát âm thanh thông báo mặc định của hệ thống

    // Dừng âm thanh khi tắt báo thức


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
