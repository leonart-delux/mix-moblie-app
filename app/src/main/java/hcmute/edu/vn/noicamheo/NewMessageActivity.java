package hcmute.edu.vn.noicamheo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import hcmute.edu.vn.noicamheo.models.Message;

public class NewMessageActivity extends AppCompatActivity {
    private AutoCompleteTextView recipientPhoneEdit;
    private TextInputEditText messageContentEdit;
    private TextInputLayout recipientPhoneLayout;
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 102;
    private static final int READ_CONTACTS_PERMISSION_REQUEST_CODE = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recipientPhoneLayout = findViewById(R.id.recipientPhoneLayout);
        recipientPhoneEdit = findViewById(R.id.recipientPhoneEdit);
        messageContentEdit = findViewById(R.id.messageContentEdit);
        Button sendButton = findViewById(R.id.sendButton);

        // Thiết lập AutoCompleteTextView cho số điện thoại
        setupPhoneNumberAutoComplete();

        sendButton.setOnClickListener(v -> sendMessage());

        requestSendSmsPermission();
    }

    private void setupPhoneNumberAutoComplete() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    READ_CONTACTS_PERMISSION_REQUEST_CODE);
            return;
        }

        ArrayList<String> phoneNumbers = getPhoneNumbersFromContacts();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, phoneNumbers);
        recipientPhoneEdit.setAdapter(adapter);
        recipientPhoneEdit.setThreshold(1); // Hiển thị gợi ý sau khi nhập 1 ký tự
    }

    private ArrayList<String> getPhoneNumbersFromContacts() {
        ArrayList<String> phoneNumbers = new ArrayList<>();
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (cursor.moveToNext()) {
                String phoneNumber = cursor.getString(numberIndex);
                if (phoneNumber != null && !phoneNumbers.contains(phoneNumber)) {
                    phoneNumbers.add(phoneNumber);
                }
            }
            cursor.close();
        }
        return phoneNumbers;
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == READ_CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupPhoneNumberAutoComplete(); 
            } else {
                Toast.makeText(this, "Contacts permission denied, no suggestions available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendMessage() {
        String recipientPhone = recipientPhoneEdit.getText().toString().trim();
        String content = messageContentEdit.getText().toString().trim();

        if (recipientPhone.isEmpty()) {
            recipientPhoneEdit.setError("Please enter recipient's phone number");
            return;
        }

        if (content.isEmpty()) {
            messageContentEdit.setError("Please enter a message");
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No permission to send SMS", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(recipientPhone, null, content, null, null);

            Message message = new Message(
                    UUID.randomUUID().toString(),
                    "current_user_id",
                    "Me",
                    recipientPhone,
                    content,
                    new Date(),
                    true
            );

            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}