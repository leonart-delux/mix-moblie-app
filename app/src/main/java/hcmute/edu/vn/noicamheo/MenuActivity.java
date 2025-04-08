package hcmute.edu.vn.noicamheo;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import hcmute.edu.vn.noicamheo.mediaplayer.MediaPlayerActivity;
import hcmute.edu.vn.noicamheo.system_setting.BatterySaverService;

public class MenuActivity extends AppCompatActivity {

    private FloatingActionButton fabMain, fabBatterySaver, fabSilentMode, fabAutoBrightness, fabDarkMode;
    private View mainLayout; // Tham chiếu đến layout chính
    private boolean isExpanded = false;
    private boolean isBatterySaverEnabled = false;
    private boolean isSilentModeEnabled = false;
    private boolean isAutoBrightnessEnabled = false;
    private boolean isDarkModeEnabled = false;
    private SensorEventListener lightSensorListener;
    private static final int REQUEST_WRITE_SETTINGS = 1;
    private static final int REQUEST_NOTIFICATION_POLICY = 2;
    private static final int REQUEST_BATTERY_OPTIMIZATION = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        // Tham chiếu đến layout chính
        mainLayout = findViewById(R.id.main);

        // Thiết lập toolbar
        MaterialToolbar toolbar = findViewById(R.id.tb_home);
        setSupportActionBar(toolbar);

        // Khởi tạo FABs
        fabMain = findViewById(R.id.fab_main);
        fabBatterySaver = findViewById(R.id.fab_battery_saver);
        fabSilentMode = findViewById(R.id.fab_silent_mode);
        fabAutoBrightness = findViewById(R.id.fab_auto_brightness);
        fabDarkMode = findViewById(R.id.fab_dark_mode);

        // Load animation
        Animation openAnim = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        Animation closeAnim = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        // Xử lý FAB chính (mở/đóng các FAB con)
        fabMain.setOnClickListener(v -> {
            if (isExpanded) {
                fabBatterySaver.startAnimation(closeAnim);
                fabSilentMode.startAnimation(closeAnim);
                fabAutoBrightness.startAnimation(closeAnim);
                fabDarkMode.startAnimation(closeAnim);

                fabBatterySaver.setVisibility(View.GONE);
                fabSilentMode.setVisibility(View.GONE);
                fabAutoBrightness.setVisibility(View.GONE);
                fabDarkMode.setVisibility(View.GONE);
            } else {
                fabBatterySaver.startAnimation(openAnim);
                fabSilentMode.startAnimation(openAnim);
                fabAutoBrightness.startAnimation(openAnim);
                fabDarkMode.startAnimation(openAnim);

                fabBatterySaver.setVisibility(View.VISIBLE);
                fabSilentMode.setVisibility(View.VISIBLE);
                fabAutoBrightness.setVisibility(View.VISIBLE);
                fabDarkMode.setVisibility(View.VISIBLE);
            }
            isExpanded = !isExpanded;
        });

        // Xử lý FAB Battery Saver
        fabBatterySaver.setOnClickListener(v -> toggleBatterySaver(fabBatterySaver));

        // Xử lý FAB Silent Mode
        fabSilentMode.setOnClickListener(v -> toggleSilentMode(fabSilentMode));

        // Xử lý FAB Auto Brightness
        fabAutoBrightness.setOnClickListener(v -> toggleAutoBrightness(fabAutoBrightness));

        // Xử lý FAB Dark Mode
        fabDarkMode.setOnClickListener(v -> toggleDarkMode(fabDarkMode));

        // Xử lý Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }

    // Thêm phương thức showPermissionDialog để hiển thị dialog yêu cầu quyền
    private void showPermissionDialog(String title, String message, Runnable onAccept, Runnable onDecline) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (onAccept != null) {
                            onAccept.run();
                        }
                    }
                })
                .setNegativeButton("Từ chối", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (onDecline != null) {
                            onDecline.run();
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "battery_channel",
                    "Battery Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for battery status");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void checkAndRequestPermissions() {
        // Kiểm tra quyền WRITE_SETTINGS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                requestWriteSettingsPermission();
            } else {
                // Kiểm tra quyền NOTIFICATION_POLICY
                checkNotificationPolicyPermission();
            }
        }
    }

    private void requestWriteSettingsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showPermissionDialog(
                    "Yêu cầu quyền điều chỉnh cài đặt hệ thống",
                    "Ứng dụng cần quyền này để điều chỉnh cài đặt hệ thống. Bạn có đồng ý cấp quyền không?",
                    () -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        Toast.makeText(this, "Vui lòng cấp quyền điều chỉnh cài đặt hệ thống", Toast.LENGTH_LONG).show();
                        startActivityForResult(intent, REQUEST_WRITE_SETTINGS);
                    },
                    () -> Toast.makeText(this, "Không thể điều chỉnh cài đặt hệ thống do thiếu quyền", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void checkNotificationPolicyPermission() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted()) {
            requestNotificationPolicyPermission();
        } else {
            // Kiểm tra quyền IGNORE_BATTERY_OPTIMIZATION
            checkBatteryOptimizationPermission();
        }
    }

    private void requestNotificationPolicyPermission() {
        showPermissionDialog(
                "Yêu cầu quyền truy cập chế độ không làm phiền",
                "Ứng dụng cần quyền này để điều khiển chế độ âm thanh. Bạn có đồng ý cấp quyền không?",
                () -> {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    Toast.makeText(this, "Vui lòng cấp quyền truy cập chế độ không làm phiền", Toast.LENGTH_LONG).show();
                    startActivityForResult(intent, REQUEST_NOTIFICATION_POLICY);
                },
                () -> Toast.makeText(this, "Không thể điều khiển chế độ âm thanh do thiếu quyền", Toast.LENGTH_SHORT).show()
        );
    }

    private void checkBatteryOptimizationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                requestBatteryOptimizationPermission();
            } else {
                // Tất cả các quyền đã được cấp
                startBatterySaverService();
            }
        } else {
            // Đối với Android dưới 6.0, không cần xin quyền này
            startBatterySaverService();
        }
    }

    private void requestBatteryOptimizationPermission() {
        showPermissionDialog(
                "Yêu cầu tắt tối ưu hóa pin",
                "Ứng dụng cần tắt tối ưu hóa pin để hoạt động hiệu quả. Bạn có đồng ý không?",
                () -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQUEST_BATTERY_OPTIMIZATION);
                    }
                },
                () -> {
                    Toast.makeText(this, "Không thể tắt tối ưu hóa pin do thiếu quyền", Toast.LENGTH_SHORT).show();
                    // Vẫn bắt đầu dịch vụ nhưng có thể sẽ bị giới hạn
                    startBatterySaverService();
                }
        );
    }

    private void startBatterySaverService() {
        Intent serviceIntent = new Intent(this, BatterySaverService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_WRITE_SETTINGS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(this)) {
                        Toast.makeText(this, "Đã cấp quyền điều chỉnh cài đặt hệ thống", Toast.LENGTH_SHORT).show();
                        // Tiếp tục kiểm tra các quyền tiếp theo
                        checkNotificationPolicyPermission();
                    } else {
                        Toast.makeText(this, "Quyền điều chỉnh cài đặt hệ thống bị từ chối", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case REQUEST_NOTIFICATION_POLICY:
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager.isNotificationPolicyAccessGranted()) {
                    Toast.makeText(this, "Đã cấp quyền truy cập chế độ không làm phiền", Toast.LENGTH_SHORT).show();
                    checkBatteryOptimizationPermission();
                } else {
                    Toast.makeText(this, "Quyền truy cập chế độ không làm phiền bị từ chối", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_BATTERY_OPTIMIZATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    if (powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                        Toast.makeText(this, "Đã tắt tối ưu hóa pin cho ứng dụng", Toast.LENGTH_SHORT).show();
                    }
                }
                // Bắt đầu dịch vụ bất kể kết quả
                startBatterySaverService();
                break;
        }
    }

    // Chức năng Battery Saver
    private void toggleBatterySaver(FloatingActionButton fabBatterySaver) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            requestWriteSettingsPermission();
            return;
        }

        isBatterySaverEnabled = !isBatterySaverEnabled;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                if (isBatterySaverEnabled) {
                    // Mở màn hình cài đặt tiết kiệm pin hệ thống
                    Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(this, "Chuyển đến cài đặt tiết kiệm pin", Toast.LENGTH_SHORT).show();
                } else {
                    // Tắt chế độ tiết kiệm pin
                    // Lưu ý: Tắt trực tiếp yêu cầu quyền WRITE_SECURE_SETTINGS (dành cho app hệ thống)
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 100);
                    Toast.makeText(this, "Đã tắt chế độ tiết kiệm pin", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Không thể điều khiển chế độ tiết kiệm pin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                isBatterySaverEnabled = !isBatterySaverEnabled; // Đặt lại trạng thái nếu thất bại
            }
        } else {
            // Giải pháp thay thế cho các phiên bản Android cũ hơn
            try {
                if (isBatterySaverEnabled) {
                    // Giảm độ sáng màn hình
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 10);
                    Toast.makeText(this, "Đã bật chế độ tiết kiệm pin", Toast.LENGTH_SHORT).show();
                } else {
                    // Khôi phục độ sáng
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 100);
                    Toast.makeText(this, "Đã tắt chế độ tiết kiệm pin", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Không thể điều khiển chế độ tiết kiệm pin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                isBatterySaverEnabled = !isBatterySaverEnabled; // Đặt lại trạng thái nếu thất bại
            }
        }

        fabBatterySaver.setImageResource(isBatterySaverEnabled ? R.drawable.ic_battery_saver : R.drawable.ic_battery_saver);
    }

    // Chức năng Auto Brightness
    private void toggleAutoBrightness(FloatingActionButton fabAutoBrightness) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            requestWriteSettingsPermission();
            return;
        }

        isAutoBrightnessEnabled = !isAutoBrightnessEnabled;

        try {
            if (isAutoBrightnessEnabled) {
                // Bật chế độ tự động điều chỉnh độ sáng của hệ thống
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                Toast.makeText(this, "Đã bật chế độ tự động điều chỉnh độ sáng", Toast.LENGTH_SHORT).show();
            } else {
                // Tắt chế độ tự động điều chỉnh độ sáng, chuyển sang chế độ thủ công
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                Toast.makeText(this, "Đã tắt chế độ tự động điều chỉnh độ sáng", Toast.LENGTH_SHORT).show();
            }

            // Cập nhật icon
            fabAutoBrightness.setImageResource(isAutoBrightnessEnabled ? R.drawable.ic_brightness : R.drawable.ic_brightness);

            // Nếu tắt chế độ tự động, hủy listener của cảm biến ánh sáng nếu có
            if (!isAutoBrightnessEnabled && lightSensorListener != null) {
                SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                sensorManager.unregisterListener(lightSensorListener);
                lightSensorListener = null;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Không thể điều chỉnh chế độ tự động sáng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isAutoBrightnessEnabled = !isAutoBrightnessEnabled; // Đặt lại trạng thái nếu thất bại
        }
    }

    // Chức năng Dark/Light Mode
    private void toggleDarkMode(FloatingActionButton fabDarkMode) {
        isDarkModeEnabled = !isDarkModeEnabled;
        if (isDarkModeEnabled) {
            // Đặt background thành màu tối
            mainLayout.setBackgroundColor(getResources().getColor(android.R.color.black));
            fabDarkMode.setImageResource(R.drawable.ic_dark_mode);
        } else {
            // Đặt background thành màu sáng
            mainLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
            fabDarkMode.setImageResource(R.drawable.ic_light_mode);
        }
    }

    // Chức năng Silent Mode
    private void toggleSilentMode(FloatingActionButton fabSilentMode) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted()) {
            showPermissionDialog(
                    "Yêu cầu quyền truy cập chế độ không làm phiền",
                    "Ứng dụng cần quyền này để bật/tắt chế độ im lặng. Bạn có đồng ý cấp quyền không?",
                    () -> {
                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        startActivityForResult(intent, REQUEST_NOTIFICATION_POLICY);
                    },
                    () -> Toast.makeText(this, "Không thể bật chế độ im lặng do thiếu quyền", Toast.LENGTH_SHORT).show()
            );
            return;
        }

        isSilentModeEnabled = !isSilentModeEnabled;
        try {
            if (isSilentModeEnabled) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                fabSilentMode.setImageResource(R.drawable.ic_silent);
                Toast.makeText(this, "Đã bật chế độ im lặng", Toast.LENGTH_SHORT).show();
            } else {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                fabSilentMode.setImageResource(R.drawable.ic_silent);
                Toast.makeText(this, "Đã tắt chế độ im lặng", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Không thể điều khiển chế độ im lặng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isSilentModeEnabled = !isSilentModeEnabled; // Đặt lại trạng thái nếu thất bại
        }
    }

    // Chuyển đổi giữa các activity
    public void switchToMediaPlayer(View view) {
        Intent intent = new Intent(this, MediaPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void switchToContact(View view) {
        Intent intent = new Intent(this, ContactActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void switchToSchedule(View view) {
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void switchToSMS(View view) {
        Intent intent = new Intent(this, SmsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy đăng ký cảm biến ánh sáng nếu đang hoạt động
        if (lightSensorListener != null) {
            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensorManager.unregisterListener(lightSensorListener);
            lightSensorListener = null;
        }
    }
}