package hcmute.edu.vn.noicamheo.fragments;

import android.app.Application;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.adapter.ContactAdapter;
import hcmute.edu.vn.noicamheo.entity.Contact;

public class ContactFragment extends Fragment {
    RecyclerView recyclerViewContact;
    private static final int REQUEST_CODE_CONTACT = 101;      // Code to request permission
    private final List<Object> contacts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        // Load components on UI
        recyclerViewContact = view.findViewById(R.id.recyclerViewContactHolder);

        // Check for permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_CONTACT);
        }

        // First load data
        contacts.addAll(loadContacts());
        addContactListHeader();

        // Append data with recycler view
        recyclerViewContact.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewContact.setAdapter(new ContactAdapter(getContext(), contacts));

        return view;
    }

    private List<Object> loadContacts() {
        List<Object> contacts = new ArrayList<>();

        // Load only display name and phone number
        Cursor cursor = requireActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC"
        );

        // Resolve data from cursor
        if (cursor != null) {
            // Get index column of name and phone in cursor
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            // Load data from each record
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                String phone = cursor.getString(phoneIndex);
                contacts.add(new Contact(name, phone));
            }

            cursor.close();
        }

        return contacts;
    }

    private void addContactListHeader() {
        // Usa Iterator instead of list because cannot modify list while using for-each
        ListIterator<Object> iterator = contacts.listIterator();
        Character lastChar = null;

        while (iterator.hasNext()) {
            Object contactElement = iterator.next();

            if (!(contactElement instanceof Contact)) {
                break;
            }

            Character firstChar = ((Contact) contactElement).getFullName().charAt(0);

            if (lastChar == null || !lastChar.equals(firstChar)) {
                lastChar = firstChar;
                iterator.previous();
                iterator.add(lastChar.toString().toUpperCase());
                iterator.next();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_CONTACT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}