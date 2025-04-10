package hcmute.edu.vn.noicamheo.fragments;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
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
    SearchView searchView;
    ContactAdapter contactAdapter;
    private final List<Object> contacts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        // Load components on UI
        recyclerViewContact = view.findViewById(R.id.recyclerViewContactHolder);
        searchView = view.findViewById(R.id.searchViewContact);

        // First load data
        contacts.addAll(loadContacts());
        addContactListHeader(contacts);

        // Append data with recycler view
        recyclerViewContact.setLayoutManager(new LinearLayoutManager(getContext()));
        contactAdapter = new ContactAdapter(getContext(), contacts);
        recyclerViewContact.setAdapter(contactAdapter);

        // Set event for search bar
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return false;
            }
        });

        return view;
    }

    // Filter list contact when search bar is being queried
    private void filterList(String newText) {
        List<Object> filteredList = new ArrayList<>();
        for (Object object: contacts) {
            if (object instanceof Contact && (
                    ((Contact) object).getFullName().toLowerCase().contains(newText.toLowerCase()) ||
                            ((Contact) object).getPhoneNumber().contains(newText)
                    )) {
                filteredList.add(object);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "No result found!", Toast.LENGTH_SHORT).show();
        } else {
            addContactListHeader(filteredList);
            contactAdapter.setFilteredList(filteredList);
        }
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

    private void addContactListHeader(List<Object> contactList) {
        // Usa Iterator instead of list because cannot modify list while using for-each
        ListIterator<Object> iterator = contactList.listIterator();
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
}