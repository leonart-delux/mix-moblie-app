package hcmute.edu.vn.noicamheo.mediaplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.noicamheo.MenuActivity;
import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.adapter.SongAdapter;
import hcmute.edu.vn.noicamheo.entity.Song;

public class MediaPlayerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private static final int REQUEST_CODE_PERMISSION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mediaplay);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mediaplay), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recycler_view_songs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Kiểm tra và yêu cầu quyền
        if (checkPermission()) {
            loadSongs();
        } else {
            requestPermission();
        }

        songAdapter = new SongAdapter(this, songList, song -> {
            Toast.makeText(this, "Playing: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(songAdapter);

        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.sv_media);
        searchView.setQuery("Music", false);
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadSongs();
        } else {
            Toast.makeText(this, "Permission denied, cannot load songs", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSongs() {
        songList.clear(); // Xóa danh sách cũ trước khi load lại
        ContentResolver contentResolver = getContentResolver();

        // Quét cả external storage
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = contentResolver.query(uri, projection, selection, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                String path = cursor.getString(pathColumn);

                if (path != null && path.endsWith(".mp3")) { // Chỉ lấy file MP3
                    songList.add(new Song(title, artist, path));
                }
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Toast.makeText(this, "No MP3 files found", Toast.LENGTH_SHORT).show();
        }

        if (songAdapter != null) {
            songAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}