package hcmute.edu.vn.noicamheo.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import hcmute.edu.vn.noicamheo.R;

public class ContactHeaderViewHolder extends RecyclerView.ViewHolder {
    TextView textViewHeader;
    public ContactHeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewHeader = itemView.findViewById(R.id.textViewHeader);
    }
}
