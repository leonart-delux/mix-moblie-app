package hcmute.edu.vn.noicamheo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;
import java.util.UUID;

import hcmute.edu.vn.noicamheo.models.Message;

public class NewMessageActivity extends AppCompatActivity {
    private TextInputEditText recipientPhoneEdit;
    private TextInputEditText messageContentEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        recipientPhoneEdit = findViewById(R.id.recipientPhoneEdit);
        messageContentEdit = findViewById(R.id.messageContentEdit);
        Button sendButton = findViewById(R.id.sendButton);

        // Setup send button click listener
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String recipientPhone = recipientPhoneEdit.getText().toString().trim();
        String content = messageContentEdit.getText().toString().trim();

        // Validate input
        if (recipientPhone.isEmpty()) {
            recipientPhoneEdit.setError("Please enter recipient's phone number");
            return;
        }

        if (content.isEmpty()) {
            messageContentEdit.setError("Please enter a message");
            return;
        }

        // Create new message
        Message message = new Message(
                UUID.randomUUID().toString(),
                "current_user_id", // In a real app, get this from user session
                "Me", // In a real app, get this from user session
                recipientPhone,
                content,
                new Date(),
                true
        );

        // In a real app, send message to backend/database
        // For now, just show success and finish activity
        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
