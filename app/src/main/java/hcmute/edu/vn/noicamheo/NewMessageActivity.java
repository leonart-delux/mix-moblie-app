package hcmute.edu.vn.noicamheo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import hcmute.edu.vn.noicamheo.entity.Contact;
import hcmute.edu.vn.noicamheo.entity.Message;

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

        ArrayList<Contact> contacts = getPhoneNumbersFromContacts();
        CustomContactAdapter adapter = new CustomContactAdapter(this, contacts);

        recipientPhoneEdit.setAdapter(adapter);
        recipientPhoneEdit.setThreshold(1);

        // Thêm listener để chỉ lấy số điện thoại khi chọn
        recipientPhoneEdit.setOnItemClickListener((parent, view, position, id) -> {
            Contact selectedContact = (Contact) parent.getItemAtPosition(position);
            recipientPhoneEdit.setText(selectedContact.getPhoneNumber());
        });
    }

    private static String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        // Xóa tất cả ký tự không phải số (khoảng trắng, dấu gạch ngang, dấu cộng, v.v.)
        String cleanedNumber = phoneNumber.replaceAll("[^0-9]", "");
        if (cleanedNumber.isEmpty()) return phoneNumber;

        // Chuẩn hóa số Việt Nam
        if (cleanedNumber.startsWith("84") && cleanedNumber.length() >= 11) {
            return "+" + cleanedNumber;
        } else if (cleanedNumber.startsWith("0")) {
            return "+84" + cleanedNumber.substring(1);
        } else if (cleanedNumber.length() >= 9 && cleanedNumber.length() <= 10) {
            return "+84" + cleanedNumber;
        }

        return cleanedNumber; // Trả về số đã làm sạch nếu không thuộc các trường hợp trên
    }

    private ArrayList<Contact> getPhoneNumbersFromContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();
        HashSet<String> uniqueNumbers = new HashSet<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            return contacts;
        }

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            while (cursor.moveToNext()) {
                String phoneNumber = cursor.getString(numberIndex);
                String fullName = cursor.getString(nameIndex);

                if (phoneNumber != null && fullName != null) {
                    String normalizedNumber = normalizePhoneNumber(phoneNumber);
                    if (!uniqueNumbers.contains(normalizedNumber)) {
                        uniqueNumbers.add(normalizedNumber);
                        contacts.add(new Contact(fullName, phoneNumber));
                    }
                }
            }
            cursor.close();
        }
        return contacts;
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
        String recipientInput = recipientPhoneEdit.getText().toString().trim();
        String content = messageContentEdit.getText().toString().trim();

        if (recipientInput.isEmpty()) {
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
            smsManager.sendTextMessage(recipientInput, null, content, null, null);

            Message message = new Message(
                    UUID.randomUUID().toString(),
                    "current_user_id",
                    "Me",
                    recipientInput,
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

    private static class CustomContactAdapter extends ArrayAdapter<Contact> {
        private final ArrayList<Contact> contacts;
        private final ArrayList<Contact> contactsFiltered;

        public CustomContactAdapter(Context context, ArrayList<Contact> contacts) {
            super(context, android.R.layout.simple_dropdown_item_1line, contacts);
            this.contacts = new ArrayList<>(contacts);
            this.contactsFiltered = new ArrayList<>(contacts);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    ArrayList<Contact> filteredList = new ArrayList<>();

                    if (constraint == null || constraint.length() == 0) {
                        filteredList.addAll(contacts);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        for (Contact contact : contacts) {
                            String fullName = contact.getFullName().toLowerCase();
                            String phoneNumber = contact.getPhoneNumber().toLowerCase();
                            String normalizedNumber = normalizePhoneNumber(contact.getPhoneNumber()).toLowerCase();

                            if (fullName.contains(filterPattern) ||
                                    phoneNumber.contains(filterPattern) ||
                                    normalizedNumber.contains(filterPattern)) {
                                filteredList.add(contact);
                            }
                        }
                    }

                    results.values = filteredList;
                    results.count = filteredList.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    contactsFiltered.clear();
                    contactsFiltered.addAll((ArrayList<Contact>) results.values);
                    notifyDataSetChanged();
                }
            };
        }

        @Override
        public int getCount() {
            return contactsFiltered.size();
        }

        @Override
        public Contact getItem(int position) {
            return contactsFiltered.get(position);
        }
    }
}