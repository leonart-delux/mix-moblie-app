package hcmute.edu.vn.noicamheo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;


import java.util.Objects;

import hcmute.edu.vn.noicamheo.adapter.ContactViewPagerAdapter;

public class ContactActivity extends AppCompatActivity {
    TabLayout contactTabLayout;
    ViewPager2 contactViewPager2;
    ContactViewPagerAdapter contactViewPagerAdapter;

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

        contactTabLayout = findViewById(R.id.contactTabLayout);
        contactViewPager2 = findViewById(R.id.contactViewPager2);
        contactViewPagerAdapter = new ContactViewPagerAdapter(this);
        contactViewPager2.setAdapter(contactViewPagerAdapter);

        contactTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                contactViewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        contactViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Objects.requireNonNull(contactTabLayout.getTabAt(position)).select();
            }
        });
    }
}