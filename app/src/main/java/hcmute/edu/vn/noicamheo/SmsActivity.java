package hcmute.edu.vn.noicamheo;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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

import hcmute.edu.vn.noicamheo.adapter.MessageAdapter;
import hcmute.edu.vn.noicamheo.entity.Message;
import hcmute.edu.vn.noicamheo.service.SmsBackgroundService;

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
        Intent serviceIntent = new Intent(this, SmsBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
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
                        refreshMessages();
                    } else {
                        Log.w(TAG, "Result not OK, code: " + result.getResultCode());
                    }
                });

        newMessageFab.setOnClickListener(v -> {
            Log.d(TAG, "FAB clicked, launching NewMessageActivity");
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

    private void requestPermissions() {
        List<String> requiredPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.READ_SMS);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.READ_CONTACTS);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.RECEIVE_SMS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!requiredPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    requiredPermissions.toArray(new String[0]),
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

        Uri uri = Telephony.Sms.CONTENT_URI;
        String selection = Telephony.Sms.DATE + " < ?";
        String[] selectionArgs = new String[]{String.valueOf(maxDate)};

        Cursor cursor = getContentResolver().query(
                uri, null, selection, selectionArgs, "date DESC LIMIT " + PAGE_SIZE
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

                // Chuẩn hóa số điện thoại ngay từ đầu
                String normalizedAddress = normalizePhoneNumber(address);
                Log.d("SmsActivity", "Original address: " + address + ", Normalized address: " + normalizedAddress);
                String senderName = getContactName(normalizedAddress);

                boolean isOutgoing = (type == Telephony.Sms.MESSAGE_TYPE_SENT);
                String senderId = isOutgoing ? "current_user_id" : normalizedAddress;
                String recipientId = isOutgoing ? normalizedAddress : "current_user_id";

                if (isOutgoing) {
                    senderName = "Me";
                }

                Message message = new Message(
                        id, senderId, senderName, recipientId, body, new Date(date), isOutgoing
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
            // Chuẩn hóa lại conversationId để đảm bảo nhất quán
            String conversationId = message.isOutgoing() ? normalizePhoneNumber(message.getRecipientId()) : normalizePhoneNumber(message.getSenderId());
            Log.d("SmsActivity", "Message from: " + message.getSenderId() + ", to: " + message.getRecipientId() + ", conversationId: " + conversationId);
            if (!latestMessages.containsKey(conversationId) ||
                    message.getTimestamp().getTime() > latestMessages.get(conversationId).getTimestamp().getTime()) {
                String displaySenderId = message.isOutgoing() ? message.getRecipientId() : message.getSenderId();
                String displaySenderName = message.isOutgoing() ? getContactName(message.getRecipientId()) : message.getSenderName();
                String displayContent = message.isOutgoing() ? "me: " + message.getContent() : message.getContent();

                Message updatedMessage = new Message(
                        message.getId(), displaySenderId, displaySenderName, message.getRecipientId(),
                        displayContent, message.getTimestamp(), message.isOutgoing()
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

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        // Xóa tất cả ký tự không phải số (khoảng trắng, dấu gạch ngang, dấu cộng, v.v.)
        String cleanedNumber = phoneNumber.replaceAll("[^0-9]", "");

        // Nếu không phải số hoặc quá ngắn, trả về nguyên bản
        if (cleanedNumber.isEmpty()) {
            return phoneNumber;
        }

        // Chuẩn hóa số Việt Nam
        if (cleanedNumber.startsWith("84") && cleanedNumber.length() >= 11) {
            // Đã ở định dạng +84, giữ nguyên
            return "+" + cleanedNumber;
        } else if (cleanedNumber.startsWith("0")) {
            // Bắt đầu bằng 0, chuyển thành +84
            return "+84" + cleanedNumber.substring(1);
        } else if (cleanedNumber.length() >= 9 && cleanedNumber.length() <= 10) {
            // Số 9-10 chữ số, giả định là số Việt Nam, thêm +84
            return "+84" + cleanedNumber;
        }

        // Nếu không thuộc các trường hợp trên, trả về nguyên bản
        return phoneNumber;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}