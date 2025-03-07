package hcmute.edu.vn.noicamheo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.noicamheo.adapter.ContactAdapter;
import hcmute.edu.vn.noicamheo.entity.Contact;

public class ContactActivity extends AppCompatActivity {
    RecyclerView recyclerViewContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        List<Contact> contacts = new ArrayList<Contact>();
        contacts.add(new Contact("Nguyen Tien Huy", "0123456789"));
        contacts.add(new Contact("Nguyen Huu Danh", "0246813579"));
        contacts.add(new Contact("Nguyen Van Vu", "0135792468"));
        contacts.add(new Contact("Nguyen Tien Huy", "0123456789"));
        contacts.add(new Contact("Nguyen Huu Danh", "0246813579"));
        contacts.add(new Contact("Nguyen Van Vu", "0135792468"));
        contacts.add(new Contact("Nguyen Tien Huy", "0123456789"));
        contacts.add(new Contact("Nguyen Huu Danh", "0246813579"));
        contacts.add(new Contact("Nguyen Van Vu", "0135792468"));
        contacts.add(new Contact("Nguyen Tien Huy", "0123456789"));
        contacts.add(new Contact("Nguyen Huu Danh", "0246813579"));
        contacts.add(new Contact("Nguyen Van Vu", "0135792468"));
        contacts.add(new Contact("Nguyen Tien Huy", "0123456789"));
        contacts.add(new Contact("Nguyen Huu Danh", "0246813579"));
        contacts.add(new Contact("Nguyen Van Vu", "0135792468"));
        contacts.add(new Contact("Nguyen Tien Huy", "0123456789"));
        contacts.add(new Contact("Nguyen Huu Danh", "0246813579"));
        contacts.add(new Contact("Nguyen Van Vu", "0135792468"));

        recyclerViewContact = findViewById(R.id.recyclerViewContactHolder);
        recyclerViewContact.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContact.setAdapter(new ContactAdapter(getApplicationContext(), contacts));
    }
}