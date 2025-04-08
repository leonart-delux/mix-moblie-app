package hcmute.edu.vn.noicamheo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    public void setFilteredList(List<Object> filteredList) {
        this.contacts = filteredList;
        notifyDataSetChanged();
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Check if position is run out of range
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        // Bind animation
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.anim_contact_list));

        // Check if holder is header item or contact item
        if (getItemViewType(position) == TYPE_HEADER) {
            ((ContactHeaderViewHolder) holder).textViewHeader.setText(contacts.get(position).toString());
            return;
        }

        // Check if this holder is opening or not
        // Also, expand or narrow the divider based on situation
        if (position == previousOpenItemPosition) {
            ((ContactViewHolder) holder).openPanel.setVisibility(View.VISIBLE);
            ((ContactViewHolder) holder).getDividerParam().removeRule(RelativeLayout.END_OF);
        } else {
            ((ContactViewHolder) holder).openPanel.setVisibility(View.GONE);
            ((ContactViewHolder) holder).getDividerParam().addRule(RelativeLayout.END_OF, R.id.imageViewAvatar);
        }

        // Set data for contact holder
        Contact contact = (Contact) (contacts.get(position));
        ((ContactViewHolder) holder).textViewFullName.setText(contact.getFullName());
        ((ContactViewHolder) holder).textViewPhone.setText(String.join(" ", "Phone", contact.getPhoneNumber()));

        // Set event for contact item to display phone number when clicked
        ((ContactViewHolder) holder).mainLayout.setOnClickListener(v -> {
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
        });

        // Set phone call event
        ((ContactViewHolder) holder).imageViewCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + contact.getPhoneNumber()));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    // Represent a contact element in contact list
    private class ContactViewHolder extends RecyclerView.ViewHolder {
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

    // Represent a contact header (first letter name group)
    private class ContactHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewHeader;
        public ContactHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewHeader = itemView.findViewById(R.id.textViewHeader);
        }
    }
}
