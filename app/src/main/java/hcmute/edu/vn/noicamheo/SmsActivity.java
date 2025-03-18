package hcmute.edu.vn.noicamheo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.noicamheo.adapters.MessageAdapter;
import hcmute.edu.vn.noicamheo.models.Message;

public class SmsActivity extends AppCompatActivity implements MessageAdapter.OnMessageClickListener {
    private MessageAdapter adapter;
    private ActivityResultLauncher<Intent> newMessageLauncher;
    private static final int SMS_AND_CONTACTS_PERMISSION_REQUEST_CODE = 100;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private static final int PAGE_SIZE = 20; // The number of message in one page
    private long lastMessageDate = Long.MAX_VALUE;

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
        layoutManager = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(adapter);

        // Add scroll listener for RecyclerView
        messagesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + 5)) {
                    loadMoreMessages();
                }
            }
        });

        // Request permission and load the first message
        requestPermissions();

        // Setup new message launcher
        newMessageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Reset và load lại từ đầu
                        lastMessageDate = Long.MAX_VALUE;
                        adapter.clearMessages();
                        loadMoreMessages();
                    }
                });

        // Setup FAB click listener
        newMessageFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewMessageActivity.class);
            newMessageLauncher.launch(intent);
        });
    }

    private void loadMoreMessages() {
        if (isLoading) return;
        isLoading = true;

        executorService.execute(() -> {
            List<Message> newMessages = loadMessagesFromSystem(lastMessageDate);

            mainHandler.post(() -> {
                if (!newMessages.isEmpty()) {
                    lastMessageDate = newMessages.get(newMessages.size() - 1).getTimestamp().getTime();
                    adapter.addMessages(newMessages);
                }
                isLoading = false;
            });
        });
    }

    private List<Message> loadMessagesFromSystem(long maxDate) {
        List<Message> messages = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return messages;
        }

        Uri uri = Telephony.Sms.Inbox.CONTENT_URI;
        String selection = Telephony.Sms.DATE + " < ?";
        String[] selectionArgs = new String[]{String.valueOf(maxDate)};

        Cursor cursor = getContentResolver().query(
                uri,
                null,
                selection,
                selectionArgs,
                "date DESC LIMIT " + PAGE_SIZE
        );

        if (cursor != null) {
            int idIndex = cursor.getColumnIndex(Telephony.Sms._ID);
            int addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
            int bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY);
            int dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE);

            while (cursor.moveToNext()) {
                String id = cursor.getString(idIndex);
                String address = cursor.getString(addressIndex);
                String body = cursor.getString(bodyIndex);
                long date = cursor.getLong(dateIndex);

                String senderName = getContactName(address);

                Message message = new Message(
                        id,
                        address,
                        senderName,
                        "current_user_id",
                        body,
                        new Date(date),
                        false
                );

                messages.add(message);
            }
            cursor.close();
        }
        return messages;
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

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_SMS,
                            Manifest.permission.READ_CONTACTS
                    },
                    SMS_AND_CONTACTS_PERMISSION_REQUEST_CODE);
        } else {
            loadMoreMessages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_AND_CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                loadMoreMessages();
            } else {
                Toast.makeText(this, "Permissions denied. Cannot load messages.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getContactName(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            return phoneNumber;
        }

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                cursor.close();
                return name;
            }
            cursor.close();
        }
        return phoneNumber;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}