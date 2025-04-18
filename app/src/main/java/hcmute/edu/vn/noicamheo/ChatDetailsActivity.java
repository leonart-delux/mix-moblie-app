package hcmute.edu.vn.noicamheo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.noicamheo.adapter.ChatMessageAdapter;
import hcmute.edu.vn.noicamheo.entity.Message;

public class ChatDetailsActivity extends AppCompatActivity {
    private static final String EXTRA_RECIPIENT_NAME = "extra_recipient_name";
    private static final String EXTRA_RECIPIENT_ID = "extra_recipient_id";
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 101;

    private ChatMessageAdapter adapter;
    private EditText messageInput;
    private RecyclerView chatRecyclerView;
    private String recipientId;
    private String currentUserId = "current_user_id";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SmsContentObserver smsObserver;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(recipientName);
        toolbar.setNavigationOnClickListener(v -> finish());

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        ImageButton sendButton = findViewById(R.id.sendButton);

        adapter = new ChatMessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(adapter);

        // Load lịch sử ban đầu
        loadChatHistory();

        sendButton.setOnClickListener(v -> sendMessage(recipientId));

        requestSendSmsPermission();

        // Đăng ký ContentObserver để theo dõi thay đổi SMS
        smsObserver = new SmsContentObserver(mainHandler);
        getContentResolver().registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                smsObserver
        );
    }

    private void requestSendSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SEND_SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied. Cannot send messages.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendMessage(String recipientId) {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) return;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No permission to send SMS", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(recipientId, null, content, null, null);

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

            messageInput.setText("");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(messageInput.getWindowToken(), 0);

            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
                if (!messages.isEmpty()) {
                    chatRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                }
            });
        });
    }

    private ArrayList<Message> loadMessagesFromSystem() {
        ArrayList<Message> messages = new ArrayList<>();

        Uri uri = Telephony.Sms.CONTENT_URI;
        String normalizedRecipientId = normalizePhoneNumber(recipientId);
        Log.d("ChatDetailsActivity", "recipientId: " + recipientId + ", normalizedRecipientId: " + normalizedRecipientId);

        // Tạo các biến thể của recipientId
        String rawNumber = recipientId.startsWith("+84") ? "0" + recipientId.substring(3) : recipientId; // Chuyển +84343883136 thành 0343883136
        String selection = "(" + Telephony.Sms.ADDRESS + " = ? OR " + Telephony.Sms.ADDRESS + " = ? OR " +
                Telephony.Sms.ADDRESS + " = ? OR " + Telephony.Sms.ADDRESS + " = ? OR " +
                Telephony.Sms.ADDRESS + " = ?) AND (" +
                Telephony.Sms.TYPE + " = ? OR " + Telephony.Sms.TYPE + " = ?)";
        String[] selectionArgs = new String[]{
                recipientId,
                normalizedRecipientId,
                recipientId.replaceAll("[^0-9]", ""),
                normalizedRecipientId.replaceAll("[^0-9]", ""),
                rawNumber, // Thêm biến thể 0343883136
                String.valueOf(Telephony.Sms.MESSAGE_TYPE_SENT),
                String.valueOf(Telephony.Sms.MESSAGE_TYPE_INBOX)
        };

        Cursor cursor = getContentResolver().query(
                uri,
                null,
                selection,
                selectionArgs,
                Telephony.Sms.DATE + " ASC"
        );

        if (cursor != null) {
            Log.d("ChatDetailsActivity", "Cursor count: " + cursor.getCount()); // Thêm log để kiểm tra số lượng tin nhắn tìm thấy
            int idIndex = cursor.getColumnIndex(Telephony.Sms._ID);
            int addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
            int bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY);
            int dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE);
            int typeIndex = cursor.getColumnIndex(Telephony.Sms.TYPE);

            while (cursor.moveToNext()) {
                String id = cursor.getString(idIndex);
                String address = cursor.getString(addressIndex);
                Log.d("ChatDetailsActivity", "Raw address from system: " + address);
                String body = cursor.getString(bodyIndex);
                long date = cursor.getLong(dateIndex);
                int type = cursor.getInt(typeIndex);

                String normalizedAddress = normalizePhoneNumber(address);
                boolean isSent = (type == Telephony.Sms.MESSAGE_TYPE_SENT);
                String senderName = isSent ? "Me" : getContactName(normalizedAddress);

                Message message = new Message(
                        id,
                        isSent ? currentUserId : normalizedAddress,
                        senderName,
                        isSent ? normalizedAddress : currentUserId,
                        body,
                        new Date(date),
                        isSent
                );

                messages.add(message);
            }
            cursor.close();
        } else {
            Log.d("ChatDetailsActivity", "Cursor is null");
        }
        return messages;
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

    // ContentObserver để theo dõi thay đổi trong SMS database
    private class SmsContentObserver extends ContentObserver {
        public SmsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // Khi có thay đổi trong SMS, reload lịch sử
            loadChatHistory();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy đăng ký ContentObserver
        if (smsObserver != null) {
            getContentResolver().unregisterContentObserver(smsObserver);
        }
        executorService.shutdown();
    }
}