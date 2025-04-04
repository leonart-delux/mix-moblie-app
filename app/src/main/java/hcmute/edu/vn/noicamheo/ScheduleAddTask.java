package hcmute.edu.vn.noicamheo;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import hcmute.edu.vn.noicamheo.broadcastReceiver.AlarmBroadcastReceiver;
import hcmute.edu.vn.noicamheo.database.DatabaseHelper;
import hcmute.edu.vn.noicamheo.entity.Task;


public class ScheduleAddTask extends AppCompatActivity {

    private EditText addTaskTitle, addTaskDescription, addTaskDate, addTaskTime;
    private Button buttonAdd;
    private DatabaseHelper databaseHelper;
    private int taskId = -1; // ID task, nếu -1 tức là đang thêm mới

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Ánh xạ các view
        addTaskTitle = findViewById(R.id.addTaskTitle);
        addTaskDescription = findViewById(R.id.addTaskDescription);
        addTaskDate = findViewById(R.id.addTaskDate);
        addTaskTime = findViewById(R.id.addTaskTime);
        buttonAdd = findViewById(R.id.buttonadd);

        databaseHelper = new DatabaseHelper(this);

        // Kiểm tra xem có dữ liệu từ Edit Task không
        Intent intent = getIntent();
        if (intent.hasExtra("taskId")) {
            taskId = intent.getIntExtra("taskId", -1);
            loadTaskData(taskId); // Load dữ liệu cũ
            buttonAdd.setText("Update Task"); // Đổi tên nút
        }

        // Chọn ngày
        addTaskDate.setOnClickListener(v -> showDatePicker());

        // Chọn giờ
        addTaskTime.setOnClickListener(v -> showTimePicker());

        // Xử lý khi bấm ADD TASK (hoặc Update Task nếu đang chỉnh sửa)
        buttonAdd.setOnClickListener(v -> saveTaskToDatabase());
    }

    private void showDatePicker() {
        Calendar myCalendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            myCalendar.set(year, month, dayOfMonth);

            // Định dạng lại ngày để loại bỏ dấu ","
            String dayOfWeek = new SimpleDateFormat("EEE", Locale.US).format(myCalendar.getTime()); // "Fri"
            String day = new SimpleDateFormat("dd", Locale.US).format(myCalendar.getTime()); // "22"
            String monthName = new SimpleDateFormat("MMM", Locale.US).format(myCalendar.getTime()); // "Mar"

            String formattedDate = dayOfWeek + " " + day + " " + monthName;  // "Fri 22 Mar"
            addTaskDate.setText(formattedDate);
        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }


    private void showTimePicker() {
        Calendar myCalendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            myCalendar.set(Calendar.MINUTE, minute);
            String timeFormat = new SimpleDateFormat("HH:mm", Locale.US).format(myCalendar.getTime());
            addTaskTime.setText(timeFormat);
        }, myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true).show();
    }

    private void loadTaskData(int taskId) {
        Task task = databaseHelper.getTaskById(taskId);
        if (task != null) {
            addTaskTitle.setText(task.getTitle());
            addTaskDescription.setText(task.getDescription());
            addTaskDate.setText(task.getDate());
            addTaskTime.setText(task.getTime());
        }
    }

    private void saveTaskToDatabase() {
        String title = addTaskTitle.getText().toString().trim();
        String description = addTaskDescription.getText().toString().trim();
        String date = addTaskDate.getText().toString().trim();
        String time = addTaskTime.getText().toString().trim();

        // Kiểm tra nếu có trường nào bị bỏ trống
        if (title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy năm hiện tại để đảm bảo năm được hiểu đúng
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String fullDateTime = date + " " + currentYear + " " + time; // VD: "Mon 25 Mar 2024 14:30"

        // Chuyển đổi ngày giờ từ chuỗi sang đối tượng Date
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy HH:mm", Locale.US);
        Calendar selectedDate = Calendar.getInstance();

        try {
            Date parsedDate = sdf.parse(fullDateTime);
            if (parsedDate != null) {
                selectedDate.setTime(parsedDate);
            } else {
                Toast.makeText(this, "Lỗi chuyển đổi ngày/giờ!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Định dạng ngày/giờ không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem ngày đã chọn có nằm trong quá khứ không
        if (isDateInPast(date, time, selectedDate)) {
            Toast.makeText(this, "Không thể đặt lịch trong quá khứ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tiến hành thêm/cập nhật Task trong database
        Task task;
        if (taskId == -1) {
            // Trường hợp thêm mới task
            task = new Task(0, title, description, date, time);
            long newTaskId = databaseHelper.addTask(task);
            if (newTaskId == -1) {
                Toast.makeText(this, "Lỗi khi thêm Task!", Toast.LENGTH_SHORT).show();
                return;
            }
            setOrScheduleAlarm(newTaskId, title, description, date, time, selectedDate.getTimeInMillis());
            Toast.makeText(this, "Task đã được thêm!", Toast.LENGTH_SHORT).show();
        } else {
            // Trường hợp cập nhật task
            task = new Task(taskId, title, description, date, time);
            databaseHelper.updateTask(task);
            setOrScheduleAlarm(taskId, title, description, date, time, selectedDate.getTimeInMillis());
            Toast.makeText(this, "Task đã được cập nhật!", Toast.LENGTH_SHORT).show();
        }

        // Kết thúc Activity sau khi lưu
        finish();
    }



    private boolean isDateInPast(String date, String time, Calendar selectedDate) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        if (selectedDate.before(today)) {
            return true; // Ngày trong quá khứ
        }
        return false;
    }


    private void setOrScheduleAlarm(long taskId, String title, String description, String date, String time, Long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmBroadcastReceiver.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("title", title);
        intent.putExtra("description", description);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String fullDate = date + " " + currentYear;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy HH:mm", Locale.US);

        try {
            Date parsedDate = sdf.parse(fullDate + " " + time);
            if (parsedDate != null) {
                calendar.setTime(parsedDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi định dạng ngày/giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        timeInMillis = calendar.getTimeInMillis();

        if (isDateInPast(date, time, calendar)) {
            Toast.makeText(this, "Không thể đặt lịch trong quá khứ!", Toast.LENGTH_SHORT).show();
            return;
        }

        intent.putExtra("time", timeInMillis);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, (int) taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, "Ứng dụng chưa có quyền đặt báo thức!", Toast.LENGTH_LONG).show();
            requestExactAlarmPermission();
            return;
        }

        SimpleDateFormat logSdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.US);
        Toast.makeText(this, "Task cần làm vào lúc: " + logSdf.format(new Date(timeInMillis)), Toast.LENGTH_SHORT).show();

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }







    /**
     * 🕒 Đặt báo thức để hiển thị thông báo khi đến giờ task
     */


    @TargetApi(Build.VERSION_CODES.S)
    private void requestExactAlarmPermission() {
        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }




}
