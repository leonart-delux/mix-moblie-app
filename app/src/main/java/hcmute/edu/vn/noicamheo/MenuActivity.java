package hcmute.edu.vn.noicamheo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import hcmute.edu.vn.noicamheo.mediaplayer.MediaPlayerActivity;

public class MenuActivity extends AppCompatActivity {

    private FloatingActionButton fabMain, btnOption1, btnOption2;
    private boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.tb_home);
        setSupportActionBar(toolbar);

        fabMain = findViewById(R.id.fab_main);
        btnOption1 = findViewById(R.id.btn_option1);
        btnOption2 = findViewById(R.id.btn_option2);

        // Load animation
        Animation openAnim = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        Animation closeAnim = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        fabMain.setOnClickListener(v -> {
            if (isExpanded) {
                btnOption1.startAnimation(closeAnim);
                btnOption2.startAnimation(closeAnim);
                btnOption1.setVisibility(View.GONE);
                btnOption2.setVisibility(View.GONE);
            } else {
                btnOption1.startAnimation(openAnim);
                btnOption2.startAnimation(openAnim);
                btnOption1.setVisibility(View.VISIBLE);
                btnOption2.setVisibility(View.VISIBLE);
            }
            isExpanded = !isExpanded;
        });
    }

    public void switchToMediaPlayer(View view) {
        Intent intent = new Intent(this, MediaPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void switchToContactPlayer(View view) {
        Intent intent = new Intent(this, ContactActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}