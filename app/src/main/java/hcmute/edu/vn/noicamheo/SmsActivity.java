package hcmute.edu.vn.noicamheo;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import android.util.Log;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final int PAGE_SIZE = 20;
    private long lastMessageDate = Long.MAX_VALUE;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        FloatingActionButton newMessageFab = findViewById(R.id.newMessageFab);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        adapter = new MessageAdapter(this);
        layoutManager = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(adapter);

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

        swipeRefreshLayout.setOnRefreshListener(this::refreshMessages);

        requestPermissions();

        newMessageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "newMessageLauncher callback triggered with result code: " + result.getResultCode());
                    if (result.getResultCode() == RESULT_OK) {
                        Log.i(TAG, "Result OK received, refreshing messages");
                        refreshMessages(); // Làm mới danh sách sau khi gửi tin nhắn
                    } else {
                        Log.w(TAG, "Result not OK, code: " + result.getResultCode());
                    }
                });

        // Thêm log vào sự kiện nhấn nút FAB
        newMessageFab.setOnClickListener(v -> {
            Log.d(TAG, "FAB clicked, launching NewMessageActivity");
            Intent intent = new Intent(this, NewMessageActivity.class);
            newMessageLauncher.launch(intent); // Mở NewMessageActivity
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

    private void loadMoreMessages() {
        if (isLoading) return;
        isLoading = true;

        executorService.execute(() -> {
            List<Message> newMessages = loadMessagesFromSystem(lastMessageDate);
            List<Message> groupedMessages = groupMessagesBySender(newMessages);

            mainHandler.post(() -> {
                if (!groupedMessages.isEmpty()) {
                    lastMessageDate = groupedMessages.get(groupedMessages.size() - 1).getTimestamp().getTime();
                    adapter.addMessages(groupedMessages);
                }
                isLoading = false;
            });
        });
    }

    private void refreshMessages() {
        isLoading = true;
        lastMessageDate = Long.MAX_VALUE;
        adapter.clearMessages();

        executorService.execute(() -> {
            List<Message> newMessages = loadMessagesFromSystem(lastMessageDate);
            List<Message> groupedMessages = groupMessagesBySender(newMessages);

            mainHandler.post(() -> {
                if (!groupedMessages.isEmpty()) {
                    lastMessageDate = groupedMessages.get(groupedMessages.size() - 1).getTimestamp().getTime();
                    adapter.addMessages(groupedMessages);
                }
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
            });
        });
    }

    private List<Message> loadMessagesFromSystem(long maxDate) {
        List<Message> messages = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return messages;
        }

        Uri uri = Telephony.Sms.CONTENT_URI; // Tải tất cả tin nhắn (Inbox + Sent)
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
            int typeIndex = cursor.getColumnIndex(Telephony.Sms.TYPE);

            while (cursor.moveToNext()) {
                String id = cursor.getString(idIndex);
                String address = cursor.getString(addressIndex);
                String body = cursor.getString(bodyIndex);
                long date = cursor.getLong(dateIndex);
                int type = cursor.getInt(typeIndex);

                // Xác định tin nhắn là gửi hay nhận
                boolean isOutgoing = (type == Telephony.Sms.MESSAGE_TYPE_SENT);
                String senderId = isOutgoing ? "current_user_id" : address;
                String recipientId = isOutgoing ? address : "current_user_id";
                String senderName = isOutgoing ? "Me" : getContactName(address);

                Message message = new Message(
                        id,
                        senderId,
                        senderName,
                        recipientId,
                        body,
                        new Date(date),
                        isOutgoing
                );
                messages.add(message);
            }
            cursor.close();
        }
        return messages;
    }

    private List<Message> groupMessagesBySender(List<Message> messages) {
        Map<String, Message> latestMessages = new HashMap<>();

        for (Message message : messages) {
            String conversationId = message.isOutgoing() ? message.getRecipientId() : message.getSenderId();
            if (!latestMessages.containsKey(conversationId) ||
                    message.getTimestamp().getTime() > latestMessages.get(conversationId).getTimestamp().getTime()) {
                String displaySenderId = message.isOutgoing() ? message.getRecipientId() : message.getSenderId();
                String displaySenderName = message.isOutgoing() ? getContactName(message.getRecipientId()) : message.getSenderName();
                String displayContent = message.isOutgoing() ? "me: " + message.getContent() : message.getContent();

                Message updatedMessage = new Message(
                        message.getId(),
                        displaySenderId,
                        displaySenderName,
                        message.getRecipientId(),
                        displayContent,
                        message.getTimestamp(),
                        message.isOutgoing()
                );
                latestMessages.put(conversationId, updatedMessage);
            }
        }

        List<Message> groupedMessages = new ArrayList<>(latestMessages.values());
        groupedMessages.sort((m1, m2) -> Long.compare(m2.getTimestamp().getTime(), m1.getTimestamp().getTime()));
        return groupedMessages;
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