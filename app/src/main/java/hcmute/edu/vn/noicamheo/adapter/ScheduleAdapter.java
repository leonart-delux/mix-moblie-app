package hcmute.edu.vn.noicamheo.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.entity.Task;


public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleHolder> {

    private Context context;
    private List<Task> tasks;
    private OnTaskActionListener taskActionListener;

    // Định nghĩa Interface (Cần khai báo là public)
    public interface OnTaskActionListener {
        void onEditTask(Task task);
        void onDeleteTask(Task task);
        void onCompleteTask(Task task);    }

    public ScheduleAdapter(Context context, List<Task> tasks, OnTaskActionListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.taskActionListener = listener;
    }

    @NonNull
    @Override
    public ScheduleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScheduleHolder(LayoutInflater.from(context).inflate(R.layout.item_task_ui, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleHolder holder, int position) {
        Task task = tasks.get(position);

        String[] dateParts = task.getDate().split(" ");
        if (dateParts.length == 3) {
            holder.dayView.setText(dateParts[0]);   // "Fri"
            holder.dateView.setText(dateParts[1]);  // "22"
            holder.monthView.setText(dateParts[2]); // "Mar"
        }

        holder.titleView.setText(task.getTitle());
        holder.timeview.setText(task.getTime());
        holder.descriptionView.setText(task.getDescription());

        holder.optionsMenu.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.inflate(R.menu.task_menu);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (taskActionListener != null) {
                    if (item.getItemId() == R.id.menu_edit) {
                        taskActionListener.onEditTask(task);
                        return true;
                    } else if (item.getItemId() == R.id.menu_delete) {
                        taskActionListener.onDeleteTask(task);
                        return true;
                    }
                }
                return false;
            });

            popupMenu.show();
        });
        holder.completeButton.setOnClickListener(view -> {
            if (taskActionListener != null) {
                taskActionListener.onCompleteTask(task);
            }
        });

    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks.clear();  // Xóa danh sách hiện tại
        this.tasks.addAll(newTasks);  // Cập nhật danh sách mới
        notifyDataSetChanged();  // Cập nhật giao diện
    }


    @Override
    public int getItemCount() {
        return tasks.size();
    }
}

