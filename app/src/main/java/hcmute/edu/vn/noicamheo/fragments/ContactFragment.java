package hcmute.edu.vn.noicamheo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.adapter.ContactAdapter;
import hcmute.edu.vn.noicamheo.entity.Contact;

public class ContactFragment extends Fragment {
    RecyclerView recyclerViewContact;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        List<Object> contacts = new ArrayList<>();
        contacts.add("B");
        contacts.add(new Contact("Bien Xuan Huy", "0987654321"));

        contacts.add("N");
        contacts.add(new Contact("Nguyen Huu Danh", "0246813579"));
        contacts.add(new Contact("Nguyen Van Vu", "0135792468"));
        contacts.add(new Contact("Nguyen Tien Huy", "0123456789"));

        recyclerViewContact = view.findViewById(R.id.recyclerViewContactHolder);
        recyclerViewContact.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewContact.setAdapter(new ContactAdapter(getContext(), contacts));

        return view;
    }
}