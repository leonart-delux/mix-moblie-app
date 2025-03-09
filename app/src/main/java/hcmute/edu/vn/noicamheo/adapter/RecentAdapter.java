package hcmute.edu.vn.noicamheo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.entity.Recent;

public class RecentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<Object> recents;

    // 2 vars below are used to distinguish between header (A, B, ...) and recent call in a recent list
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_HEADER = 0;

    // Save the previous position of opening item (contact that displaying phone number
    int previousOpenItemPosition = -1;

    public RecentAdapter(Context context, List<Object> recents) {
        this.context = context;
        this.recents = recents;
    }

    @Override
    public int getItemViewType(int position) {
        return recents.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_recent, parent, false);
            return new RecentViewHolder(view);
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

        // Check if holder is header item or contact item
        if (getItemViewType(position) == TYPE_HEADER) {
            ((ContactHeaderViewHolder) holder).textViewHeader.setText(recents.get(position).toString());
            return;
        }

        // Check if this holder is opening or not
        // Also, expand or narrow the divider based on situation
        if (position == previousOpenItemPosition) {
            ((RecentViewHolder) holder).openPanel.setVisibility(View.VISIBLE);
            ((RecentViewHolder) holder).getDividerParam().removeRule(RelativeLayout.END_OF);
        } else {
            ((RecentViewHolder) holder).openPanel.setVisibility(View.GONE);
            ((RecentViewHolder) holder).getDividerParam().addRule(RelativeLayout.END_OF, R.id.imageViewAvatar);
        }

        // Set data for recent view holder
        Recent recent = (Recent) (recents.get(position));
        ((RecentViewHolder) holder).textViewRecentCall.setText(recent.getFullName());
        ((RecentViewHolder) holder).textViewPhone.setText(String.join(" ", "Phone", recent.getPhoneNumber()));

        switch (recent.geteRecentCallType()) {
            case TYPE_MISSED:
                ((RecentViewHolder) holder).imageViewCallType.setImageDrawable(
                        ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_baseline_call_missed_24)
                );
                break;

            case TYPE_RECEIVED:
                ((RecentViewHolder) holder).imageViewCallType.setImageDrawable(
                        ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_baseline_call_received_24)
                );
                break;

            case TYPE_MISSED_OUTGOING:
                ((RecentViewHolder) holder).imageViewCallType.setImageDrawable(
                        ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_baseline_call_missed_outgoing_24)
                );
                break;

            default:
                ((RecentViewHolder) holder).imageViewCallType.setImageDrawable(
                        ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_baseline_call_made_24)
                );
                break;
        }


        // If not in contact -> hide phone number cause it in name already
        if (recent.isInContact()) {
            ((RecentViewHolder) holder).textViewPhone.setVisibility(View.VISIBLE);
        } else {
            ((RecentViewHolder) holder).textViewPhone.setVisibility(View.GONE);
        }

        // Set event for contact item to display phone number when clicked
        ((RecentViewHolder) holder).textViewRecentCall.setOnClickListener(v -> {
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
    }

    @Override
    public int getItemCount() {
        return recents.size();
    }
}
