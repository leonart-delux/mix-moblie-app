package hcmute.edu.vn.noicamheo.adapter;

import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import hcmute.edu.vn.noicamheo.R;

public class ContactViewHolder extends RecyclerView.ViewHolder {
    RelativeLayout mainLayout;
    TextView textViewFullName;
    RelativeLayout openPanel;
    TextView textViewPhone;
    ImageView imageViewCall;
    ImageView imageViewMessage;
    LinearLayout divider;
    RelativeLayout.LayoutParams params;


    public ContactViewHolder(@NonNull View itemView) {
        super(itemView);
        mainLayout = itemView.findViewById(R.id.itemContact);
        textViewFullName = itemView.findViewById(R.id.textViewFullName);
        openPanel = itemView.findViewById(R.id.relativeLayoutOpeningPanel);
        textViewPhone = itemView.findViewById(R.id.textViewPhone);
        imageViewCall = itemView.findViewById(R.id.imageViewCall);
        imageViewMessage = itemView.findViewById(R.id.imageViewMessage);
        divider = itemView.findViewById(R.id.contactItemDivider);
        params = (RelativeLayout.LayoutParams) divider.getLayoutParams();
    }

    public RelativeLayout.LayoutParams getDividerParam() {
        return params;
    }
}
