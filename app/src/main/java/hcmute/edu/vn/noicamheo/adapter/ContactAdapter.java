package hcmute.edu.vn.noicamheo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.entity.Contact;

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<Object> contacts;

    // 2 vars below are used to distinguish between header (A, B, ...) and contact in a contact list
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_HEADER = 0;

    // Save the previous position of opening item (contact that displaying phone number
    int previousOpenItemPosition = -1;

    public ContactAdapter(Context context, List<Object> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    public int getItemViewType(int position) {
        return contacts.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
            return new ContactViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_header_contact, parent, false);
            return new ContactHeaderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Get absolute position of list
        int absolutePosition = holder.getAbsoluteAdapterPosition();

        // Check if position is run out of range
        if (absolutePosition == RecyclerView.NO_POSITION) {
            return;
        }

        // Check if holder is header item or contact item
        if (getItemViewType(absolutePosition) == TYPE_HEADER) {
            ((ContactHeaderViewHolder) holder).textViewHeader.setText(contacts.get(position).toString());
            return;
        }

        // Check if this holder is opening or not
        // Also, expand or narrow the divider based on situation
        if (absolutePosition == previousOpenItemPosition) {
            ((ContactViewHolder) holder).openPanel.setVisibility(View.VISIBLE);
            ((ContactViewHolder) holder).getDividerParam().removeRule(RelativeLayout.END_OF);
        } else {
            ((ContactViewHolder) holder).openPanel.setVisibility(View.GONE);
            ((ContactViewHolder) holder).getDividerParam().addRule(RelativeLayout.END_OF, R.id.imageViewAvatar);
        }

        // Set data for contact holder
        Contact contact = (Contact) (contacts.get(absolutePosition));
        ((ContactViewHolder) holder).textViewFullName.setText(contact.getFullName());
        ((ContactViewHolder) holder).textViewPhone.setText(String.join(" ", "Phone", contact.getPhoneNumber()));

        // Set event for contact item to display phone number when clicked
        ((ContactViewHolder) holder).textViewFullName.setOnClickListener(v -> {
            // Temporary save the previous clicked position
            int temp = previousOpenItemPosition;
            // Update new value for the variable below
            previousOpenItemPosition = absolutePosition;

            // In case 'previous' and current is the same --> close
            if (temp == previousOpenItemPosition) {
                previousOpenItemPosition = -1;
            }

            // Notify adapter to change view
            notifyItemChanged(temp);
            notifyItemChanged(previousOpenItemPosition);
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}
