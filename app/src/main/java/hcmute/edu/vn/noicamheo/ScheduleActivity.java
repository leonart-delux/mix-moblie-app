package hcmute.edu.vn.noicamheo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import hcmute.edu.vn.noicamheo.entity.Task;
import hcmute.edu.vn.noicamheo.database.DatabaseHelper;


public class ScheduleActivity extends AppCompatActivity implements ScheduleAdapter.OnTaskActionListener {

    private RecyclerView recyclerView;
    private ScheduleAdapter scheduleAdapter;
    private DatabaseHelper databaseHelper;
    private List<Task> taskList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Ánh xạ RecyclerView
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo database
        databaseHelper = new DatabaseHelper(this);

        // Cập nhật dữ liệu vào RecyclerView
        loadTasks();

        // Xử lý nút hiển thị lịch
        Button btnShowCalendar = findViewById(R.id.btnShowCalendar);
        btnShowCalendar.setOnClickListener(v -> showCalendarDialog());

        // Xử lý nút ADD TASK
        Button btnAdd = findViewById(R.id.buttonaddtask);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ScheduleActivity.this, ScheduleAddTask.class);
            startActivity(intent);
        });
    }

    private void loadTasks() {
        taskList = databaseHelper.getAllTasks();

        // Kiểm tra log dữ liệu lấy từ database
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

        // Ánh xạ CalendarView
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        List<EventDay> taskEvents = getTaskEvents();

        if (!taskEvents.isEmpty()) {
            calendarView.setEvents(taskEvents); // Đánh dấu ngày có task
        }

        // Bắt sự kiện khi chọn ngày trên lịch
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar selectedDate = eventDay.getCalendar();

            // Chuyển ngày thành định dạng lưu trong database
            SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM", Locale.US);
            String selectedDateString = sdf.format(selectedDate.getTime());

            // Log để kiểm tra ngày đã chọn từ CalendarView
            Log.d("CalendarCheck", "Selected Date: " + selectedDateString);

            // Truy vấn task theo ngày đã chọn
            List<Task> filteredTasks = databaseHelper.getTasksByDate(selectedDateString);

            // Kiểm tra danh sách task có lấy được không
            if (filteredTasks.isEmpty()) {
                Log.d("DatabaseQuery", "Không có task nào cho ngày: " + selectedDateString);
                Toast.makeText(this, "Không có task nào cho ngày này!", Toast.LENGTH_SHORT).show();
            } else {
                for (Task task : filteredTasks) {
                    Log.d("DatabaseQuery", "Task Found: " + task.getTitle() + " | Date: " + task.getDate());
                }
                scheduleAdapter.updateTasks(filteredTasks); // Cập nhật RecyclerView
            }

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }


    //phương thức này để lấy danh sách các ngày có task từ Database.
    private List<EventDay> getTaskEvents() {
        List<Task> taskList = databaseHelper.getAllTasks(); // Lấy tất cả task
        List<EventDay> taskEvents = new ArrayList<>();


        for (Task task : taskList) {
            try {
                // Chuyển đổi ngày từ String thành Calendar
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy", Locale.US);
                Date date = sdf.parse(task.getDate() + " " + Calendar.getInstance().get(Calendar.YEAR));

                if (date != null) {
                    calendar.setTime(date);

                    // Tạo EventDay với icon dot đỏ (hoặc thay bằng drawable khác)
                    taskEvents.add(new EventDay(calendar, R.drawable.ic_dotcalender));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return taskEvents;
    }


    private void showCompleteDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ScheduleActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_dialog_complete_task, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }
    @Override
    public void onCompleteTask(Task task) {
        showCompleteDialog(task);
    }

    private void showCompleteDialog(Task task) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ScheduleActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_dialog_complete_task, null);
        bottomSheetDialog.setContentView(view);

        Button closeButton = view.findViewById(R.id.buttonclose);
        closeButton.setOnClickListener(v -> {
            // Xóa Task khỏi database và danh sách
            databaseHelper.deleteTask(task.getId());
            taskList.remove(task);
            scheduleAdapter.notifyDataSetChanged();

            // Đóng dialog
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadTasks(); // Refresh RecyclerView khi quay lại màn hình
    }

    // Xử lý khi chọn EDIT task
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

    // Xử lý khi chọn DELETE task
    @Override
    public void onDeleteTask(Task task) {
        databaseHelper.deleteTask(task.getId());
        taskList.remove(task);
        scheduleAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Task đã được xoá", Toast.LENGTH_SHORT).show();
    }


}
