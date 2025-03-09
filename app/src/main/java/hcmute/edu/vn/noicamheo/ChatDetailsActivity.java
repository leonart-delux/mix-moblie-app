package hcmute.edu.vn.noicamheo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import hcmute.edu.vn.noicamheo.adapters.ChatMessageAdapter;
import hcmute.edu.vn.noicamheo.models.Message;

public class ChatDetailsActivity extends AppCompatActivity {
    private static final String EXTRA_RECIPIENT_NAME = "extra_recipient_name";
    private static final String EXTRA_RECIPIENT_ID = "extra_recipient_id";

    private ChatMessageAdapter adapter;
    private EditText messageInput;
    private RecyclerView chatRecyclerView;

    public static Intent newIntent(Context context, String recipientName, String recipientId) {
        Intent intent = new Intent(context, ChatDetailsActivity.class);
        intent.putExtra(EXTRA_RECIPIENT_NAME, recipientName);
        intent.putExtra(EXTRA_RECIPIENT_ID, recipientId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_details);

        String recipientName = getIntent().getStringExtra(EXTRA_RECIPIENT_NAME);
        String recipientId = getIntent().getStringExtra(EXTRA_RECIPIENT_ID);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(recipientName);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        ImageButton sendButton = findViewById(R.id.sendButton);

        // Setup RecyclerView
        adapter = new ChatMessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(adapter);

        // Load dummy messages (in a real app, load from database)
        loadDummyMessages();

        // Setup send button
        sendButton.setOnClickListener(v -> sendMessage(recipientId));
    }

    private void sendMessage(String recipientId) {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) return;

        // Create and add new message
        Message message = new Message(
                UUID.randomUUID().toString(),
                "current_user_id", // In a real app, get from user session
                "Me", // In a real app, get from user session
                recipientId,
                content,
                new Date(),
                true
        );

        adapter.addMessage(message);
        chatRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);

        // Clear input and hide keyboard
        messageInput.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(messageInput.getWindowToken(), 0);
    }

    private void loadDummyMessages() {
        // In a real app, load messages from database
        ArrayList<Message> messages = new ArrayList<>();
        // Add some dummy messages for demonstration
        messages.add(new Message(
                UUID.randomUUID().toString(),
                "other_user_id",
                "John",
                "current_user_id",
                "Hey, how are you?",
                new Date(System.currentTimeMillis() - 3600000),
                false
        ));
        messages.add(new Message(
                UUID.randomUUID().toString(),
                "current_user_id",
                "Me",
                "other_user_id",
                "I'm good, thanks! How about you?",
                new Date(System.currentTimeMillis() - 3000000),
                true
        ));
        adapter.setMessages(messages);
    }
}
