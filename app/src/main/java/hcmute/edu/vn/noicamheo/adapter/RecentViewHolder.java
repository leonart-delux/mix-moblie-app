package hcmute.edu.vn.noicamheo.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import hcmute.edu.vn.noicamheo.R;

public class RecentViewHolder extends RecyclerView.ViewHolder {
    ImageView imageViewCallType;
    TextView textViewRecentCall;
    RelativeLayout openPanel;
    TextView textViewPhone;
    ImageView imageViewCall;
    ImageView imageViewMessage;
    LinearLayout divider;
    RelativeLayout.LayoutParams params;

    public RecentViewHolder(@NonNull View itemView) {
        super(itemView);
        imageViewCallType = itemView.findViewById(R.id.imageViewCallType);
        textViewRecentCall = itemView.findViewById(R.id.textViewRecentCall);
        openPanel = itemView.findViewById(R.id.relativeLayoutOpeningPanel);
        textViewPhone = itemView.findViewById(R.id.textViewPhone);
        imageViewCall = itemView.findViewById(R.id.imageViewCall);
        imageViewMessage = itemView.findViewById(R.id.imageViewMessage);
        divider = itemView.findViewById(R.id.recentItemDivider);
        params = (RelativeLayout.LayoutParams) divider.getLayoutParams();
    }

    public RelativeLayout.LayoutParams getDividerParam() {
        return params;
    }
}
