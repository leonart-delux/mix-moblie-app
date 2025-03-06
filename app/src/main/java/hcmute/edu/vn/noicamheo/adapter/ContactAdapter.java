package hcmute.edu.vn.noicamheo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.entity.Contact;

public class ContactAdapter extends RecyclerView.Adapter<ContactViewHolder> {
    Context context;
    List<Contact> contacts;

    // Save the previous position of opening item (contact that displaying phone number_
    int previousOpenItemPosition = -1;

    public ContactAdapter(Context context, List<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactViewHolder(LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        // Check if this holder is opening or not
        // Also, expand or narrow the divider based on situation
        if (position == previousOpenItemPosition) {
            holder.openPanel.setVisibility(View.VISIBLE);
            holder.getDividerParam().removeRule(RelativeLayout.END_OF);
        } else {
            holder.openPanel.setVisibility(View.GONE);
            holder.getDividerParam().addRule(RelativeLayout.END_OF, R.id.imageViewAvatar);
        }

        holder.textViewFullName.setText(contacts.get(position).getFullName());
        holder.textViewPhone.setText(String.join(" ", "Phone", contacts.get(position).getPhoneNumber()));

        // Set event for contact item to display phone number when clicked
        holder.textViewFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Temporary save the previous clicked position
                int temp = previousOpenItemPosition;
                // Update new value for the variable below
                previousOpenItemPosition = position;
                // In case 'previous' and current is the same --> close
                if (temp == previousOpenItemPosition) {
                    previousOpenItemPosition = -1;
                }
                // Notify adapter to change view
                notifyItemChanged(temp);
                notifyItemChanged(previousOpenItemPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}
