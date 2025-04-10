package hcmute.edu.vn.noicamheo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.noicamheo.adapter.ScheduleAdapter;
import hcmute.edu.vn.noicamheo.database.DatabaseHelper;
import hcmute.edu.vn.noicamheo.entity.Task;

public class ScheduleActivity extends AppCompatActivity implements ScheduleAdapter.OnTaskActionListener {

    private static final int REQUEST_CODE_PERMISSIONS = 1001;

    private RecyclerView recyclerView;
    private ScheduleAdapter scheduleAdapter;
    private DatabaseHelper databaseHelper;
    private List<Task> taskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        checkAndRequestPermissions();

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new DatabaseHelper(this);

        loadTasks();

        Button btnShowCalendar = findViewById(R.id.btnShowCalendar);
        btnShowCalendar.setOnClickListener(v -> showCalendarDialog());

        Button btnAdd = findViewById(R.id.buttonaddtask);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ScheduleActivity.this, ScheduleAddTask.class);
            startActivity(intent);
        });
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // Runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_CODE_PERMISSIONS);
        }

        // SCHEDULE_EXACT_ALARM (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    private void loadTasks() {
        taskList = databaseHelper.getAllTasks();
        for (Task task : taskList) {
            Log.d("DatabaseCheck", "Task: " + task.getTitle() + " - Date: " + task.getDate());
        }
        scheduleAdapter = new ScheduleAdapter(this, taskList, this);
        recyclerView.setAdapter(scheduleAdapter);
    }

    private void showCalendarDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ScheduleActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_view_calender, null);
        bottomSheetDialog.setContentView(view);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        List<EventDay> taskEvents = getTaskEvents();

        if (!taskEvents.isEmpty()) {
            calendarView.setEvents(taskEvents);
        }

        calendarView.setOnDayClickListener(eventDay -> {
            Calendar selectedDate = eventDay.getCalendar();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM", Locale.US);
            String selectedDateString = sdf.format(selectedDate.getTime());
            Log.d("CalendarCheck", "Selected Date: " + selectedDateString);

            List<Task> filteredTasks = databaseHelper.getTasksByDate(selectedDateString);
            if (filteredTasks.isEmpty()) {
                Toast.makeText(this, "Không có task nào cho ngày này!", Toast.LENGTH_SHORT).show();
            } else {
                scheduleAdapter.updateTasks(filteredTasks);
            }

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private List<EventDay> getTaskEvents() {
        List<Task> taskList = databaseHelper.getAllTasks();
        List<EventDay> taskEvents = new ArrayList<>();

        for (Task task : taskList) {
            try {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy", Locale.US);
                Date date = sdf.parse(task.getDate() + " " + Calendar.getInstance().get(Calendar.YEAR));

                if (date != null) {
                    calendar.setTime(date);
                    taskEvents.add(new EventDay(calendar, R.drawable.ic_dotcalender));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return taskEvents;
    }

    private void showCompleteDialog(Task task) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ScheduleActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_dialog_complete_task, null);
        bottomSheetDialog.setContentView(view);

        Button closeButton = view.findViewById(R.id.buttonclose);
        closeButton.setOnClickListener(v -> {
            databaseHelper.deleteTask(task.getId());
            taskList.remove(task);
            scheduleAdapter.notifyDataSetChanged();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    @Override
    public void onCompleteTask(Task task) {
        showCompleteDialog(task);
    }

    @Override
    public void onEditTask(Task task) {
        Intent intent = new Intent(this, ScheduleAddTask.class);
        intent.putExtra("taskId", task.getId());
        intent.putExtra("taskTitle", task.getTitle());
        intent.putExtra("taskDescription", task.getDescription());
        intent.putExtra("taskDate", task.getDate());
        intent.putExtra("taskTime", task.getTime());
        startActivity(intent);
    }

    @Override
    public void onDeleteTask(Task task) {
        databaseHelper.deleteTask(task.getId());
        taskList.remove(task);
        scheduleAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Task đã được xoá", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    // Optional: handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Log hoặc xử lý nếu muốn
    }
}
