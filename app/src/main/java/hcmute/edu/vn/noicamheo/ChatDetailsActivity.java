package hcmute.edu.vn.noicamheo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.noicamheo.adapters.ChatMessageAdapter;
import hcmute.edu.vn.noicamheo.models.Message;

public class ChatDetailsActivity extends AppCompatActivity {
    private static final String EXTRA_RECIPIENT_NAME = "extra_recipient_name";
    private static final String EXTRA_RECIPIENT_ID = "extra_recipient_id";

    private ChatMessageAdapter adapter;
    private EditText messageInput;
    private RecyclerView chatRecyclerView;
    private String recipientId;
    private String currentUserId = "current_user_id";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
        recipientId = getIntent().getStringExtra(EXTRA_RECIPIENT_ID);

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

        // Load chat history from system SMS
        loadChatHistory();

        // Setup send button
        sendButton.setOnClickListener(v -> sendMessage(recipientId));
    }

    private void sendMessage(String recipientId) {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) return;

        // Create and add new message
        Message message = new Message(
                UUID.randomUUID().toString(),
                currentUserId,
                "Me",
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

    private void loadChatHistory() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        executorService.execute(() -> {
            ArrayList<Message> messages = loadMessagesFromSystem();
            mainHandler.post(() -> {
                adapter.setMessages(messages);
                chatRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
            });
        });
    }

    private ArrayList<Message> loadMessagesFromSystem() {
        ArrayList<Message> messages = new ArrayList<>();

        // Query messages from system SMS
        Uri uri = Telephony.Sms.CONTENT_URI;
        String selection = Telephony.Sms.ADDRESS + " = ?";
        String[] selectionArgs = new String[]{recipientId};

        Cursor cursor = getContentResolver().query(
                uri,
                null,
                selection,
                selectionArgs,
                Telephony.Sms.DATE + " ASC" // Sort by date in ascending order
        );

        if (cursor != null) {
            int idIndex = cursor.getColumnIndex(Telephony.Sms._ID);
            int addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
            int bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY);
            int dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE);
            int typeIndex = cursor.getColumnIndex(Telephony.Sms.TYPE);

            while (cursor.moveToNext()) {
                String id = cursor.getString(idIndex);
                String address = cursor.getString(addressIndex);
                String body = cursor.getString(bodyIndex);
                long date = cursor.getLong(dateIndex);
                int type = cursor.getInt(typeIndex);

                boolean isSent = (type == Telephony.Sms.MESSAGE_TYPE_SENT);
                String senderName = isSent ? "Me" : recipientId;

                Message message = new Message(
                        id,
                        isSent ? currentUserId : address,
                        senderName,
                        isSent ? address : currentUserId,
                        body,
                        new Date(date),
                        isSent
                );

                messages.add(message);
            }
            cursor.close();
        }
        return messages;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}