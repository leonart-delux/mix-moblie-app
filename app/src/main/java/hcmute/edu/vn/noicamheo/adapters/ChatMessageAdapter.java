package hcmute.edu.vn.noicamheo.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.models.Message;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {
    private List<Message> messages;
    private final SimpleDateFormat timeFormat;

    public ChatMessageAdapter() {
        this.messages = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        private final CardView messageCard;
        private final TextView messageText;
        private final TextView timeText;

        ChatMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageCard = itemView.findViewById(R.id.messageCard);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
        }

        void bind(Message message) {
            messageText.setText(message.getContent());
            timeText.setText(timeFormat.format(message.getTimestamp()));

            // Adjust layout for sent vs received messages
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            
            if (message.isOutgoing()) {
                params.gravity = Gravity.END;
                messageCard.setCardBackgroundColor(itemView.getContext().getResources()
                        .getColor(android.R.color.holo_blue_light));
                messageText.setTextColor(itemView.getContext().getResources()
                        .getColor(android.R.color.white));
                params.setMargins(64, 4, 8, 4); // left, top, right, bottom
            } else {
                params.gravity = Gravity.START;
                messageCard.setCardBackgroundColor(itemView.getContext().getResources()
                        .getColor(android.R.color.white));
                messageText.setTextColor(itemView.getContext().getResources()
                        .getColor(android.R.color.black));
                params.setMargins(8, 4, 64, 4); // left, top, right, bottom
            }
            messageCard.setLayoutParams(params);
        }
    }
}
