package hcmute.edu.vn.noicamheo;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import hcmute.edu.vn.noicamheo.adapters.MessageAdapter;
import hcmute.edu.vn.noicamheo.models.Message;

public class SmsActivity extends AppCompatActivity implements MessageAdapter.OnMessageClickListener {
    private MessageAdapter adapter;
    private ActivityResultLauncher<Intent> newMessageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        RecyclerView messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        FloatingActionButton newMessageFab = findViewById(R.id.newMessageFab);

        // Setup RecyclerView
        adapter = new MessageAdapter(this);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(adapter);

        // Load dummy messages (in a real app, load from database)
        loadDummyMessages();

        // Setup new message launcher
        newMessageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // In a real app, refresh messages from database
                        loadDummyMessages();
                    }
                });

        // Setup FAB click listener
        newMessageFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewMessageActivity.class);
            newMessageLauncher.launch(intent);
        });
    }

    @Override
    public void onMessageClick(Message message) {
        Intent intent = ChatDetailsActivity.newIntent(
                this,
                message.getSenderName(),
                message.getSenderId()
        );
        startActivity(intent);
    }

    private void loadDummyMessages() {
        // In a real app, load messages from database
        ArrayList<Message> messages = new ArrayList<>();

        // Add some dummy messages for demonstration
        messages.add(new Message(
                UUID.randomUUID().toString(),
                "user1",
                "John Doe",
                "current_user_id",
                "Hey, how's it going?",
                new Date(System.currentTimeMillis() - 3600000),
                false
        ));

        messages.add(new Message(
                UUID.randomUUID().toString(),
                "user2",
                "Jane Smith",
                "current_user_id",
                "Are we still meeting tomorrow?",
                new Date(System.currentTimeMillis() - 7200000),
                false
        ));

        messages.add(new Message(
                UUID.randomUUID().toString(),
                "user3",
                "Mike Johnson",
                "current_user_id",
                "Thanks for the help yesterday!",
                new Date(System.currentTimeMillis() - 86400000),
                false
        ));

        adapter.setMessages(messages);
    }
}
