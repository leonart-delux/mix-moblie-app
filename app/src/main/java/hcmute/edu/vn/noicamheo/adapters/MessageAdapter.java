package hcmute.edu.vn.noicamheo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private final OnMessageClickListener listener;
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateTimeFormat;

    public interface OnMessageClickListener {
        void onMessageClick(Message message);
    }

    public MessageAdapter(OnMessageClickListener listener) {
        this.messages = new ArrayList<>();
        this.listener = listener;
        this.timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        this.dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.getDefault()); // Định dạng mới
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
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
        messages.add(0, message);
        notifyItemInserted(0);
    }

    public void addMessages(List<Message> newMessages) {
        int startPosition = messages.size();
        messages.addAll(newMessages);
        notifyItemRangeInserted(startPosition, newMessages.size());
    }

    public void clearMessages() {
        int size = messages.size();
        messages.clear();
        notifyItemRangeRemoved(0, size);
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView senderNameText;
        private final TextView messagePreviewText;
        private final TextView messageTimeText;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderNameText = itemView.findViewById(R.id.senderNameText);
            messagePreviewText = itemView.findViewById(R.id.messagePreviewText);
            messageTimeText = itemView.findViewById(R.id.messageTimeText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMessageClick(messages.get(position));
                }
            });
        }

        void bind(Message message) {
            senderNameText.setText(message.getSenderName());
            messagePreviewText.setText(message.getContent());

            messageTimeText.setText(dateTimeFormat.format(message.getTimestamp()));
        }
    }
}