package hcmute.edu.vn.noicamheo.adapter;


import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import hcmute.edu.vn.noicamheo.R;

public class ScheduleHolder extends RecyclerView.ViewHolder {

    public TextView dateView, dayView, monthView, titleView, timeview, descriptionView, completeButton;
    public ImageView optionsMenu; // ImageView để mở menu
    private OnMenuClickListener listener;

    public ScheduleHolder(@NonNull View itemView) {
        super(itemView);

        dayView = itemView.findViewById(R.id.day);
        dateView = itemView.findViewById(R.id.date);
        monthView = itemView.findViewById(R.id.month);
        titleView = itemView.findViewById(R.id.title);
        timeview = itemView.findViewById(R.id.time);
        descriptionView = itemView.findViewById(R.id.description);
        optionsMenu = itemView.findViewById(R.id.imageView5); // Ánh xạ ImageView
        completeButton = itemView.findViewById(R.id.status);

        // Bắt sự kiện nhấn vào ImageView để hiển thị Popup Menu
        optionsMenu.setOnClickListener(view -> showPopupMenu(view));
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.task_menu); // Gán menu từ res/menu/task_menu.xml

        // Xử lý sự kiện khi chọn item trong menu
        popupMenu.setOnMenuItemClickListener(item -> {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    int itemId = item.getItemId();

                    if (itemId == R.id.menu_edit) {
                        listener.onEditClick(position);
                        return true;
                    } else if (itemId == R.id.menu_delete) {
                        listener.onDeleteClick(position);
                        return true;
                    }
                }
            }
            return false;
        });
        popupMenu.show();
    }

    // Interface để giao tiếp với Adapter
    public interface OnMenuClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.listener = listener;
    }
}

