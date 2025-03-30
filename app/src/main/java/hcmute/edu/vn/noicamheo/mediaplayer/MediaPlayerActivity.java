package hcmute.edu.vn.noicamheo.mediaplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.adapter.SongAdapter;
import hcmute.edu.vn.noicamheo.entity.Song;

public class MediaPlayerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private static final int REQUEST_CODE_PERMISSION = 123;
    private ExoPlayer player;
    private TextView tvSongTitle, tvArtistName;
    private ImageButton btnPlayPause, btnPrevious, btnNext;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvTotalTime;
    private Handler handler = new Handler();
    private int currentSongIndex = -1;

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
        tvSongTitle = findViewById(R.id.tv_song_title);
        tvArtistName = findViewById(R.id.tv_artist_name);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        seekBar = findViewById(R.id.seek_bar);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);

        player = new ExoPlayer.Builder(this).build();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    seekBar.setMax((int) player.getDuration());
                    tvTotalTime.setText(formatTime((int) player.getDuration()));
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        if (checkPermission()) {
            loadSongs();
        } else {
            requestPermission();
        }

        songAdapter = new SongAdapter(this, songList, this::playSong);
        recyclerView.setAdapter(songAdapter);

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnPrevious.setOnClickListener(v -> playPreviousSong());
        btnNext.setOnClickListener(v -> playNextSong());
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
        songList.clear();
        ContentResolver contentResolver = getContentResolver();
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

                if (path != null && path.endsWith(".mp3")) {
                    songList.add(new Song(title, artist, path));
                }
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Toast.makeText(this, "No MP3 files found", Toast.LENGTH_SHORT).show();
        }

        if (songAdapter != null) {
            songAdapter.notifyDataSetChanged();
        }
    }

    private void playSong(Song song) {
        // Cập nhật currentSongIndex dựa trên bài hát được chọn
        currentSongIndex = songList.indexOf(song);

        tvSongTitle.setText(song.getTitle());
        tvArtistName.setText(song.getArtist());

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(song.getPath()));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        btnPlayPause.setImageResource(R.drawable.ic_pause);

        handler.removeCallbacks(updateSeekBar);
        handler.post(updateSeekBar);
    }

    private void playPreviousSong() {
        if (songList.isEmpty()) {
            Toast.makeText(this, "No songs available", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentSongIndex <= 0) {
            currentSongIndex = songList.size() - 1;
        } else {
            currentSongIndex--;
        }
        playSong(songList.get(currentSongIndex));
    }

    private void playNextSong() {
        if (songList.isEmpty()) {
            Toast.makeText(this, "No songs available", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentSongIndex >= songList.size() - 1) {
            currentSongIndex = 0;
        } else {
            currentSongIndex++;
        }
        playSong(songList.get(currentSongIndex));
    }

    private void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play);
        } else {
            player.play();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            handler.post(updateSeekBar);
        }
    }

    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                seekBar.setProgress((int) player.getCurrentPosition());
                tvCurrentTime.setText(formatTime((int) player.getCurrentPosition()));
                handler.postDelayed(this, 500);
            }
        }
    };

    private String formatTime(int millis) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
        handler.removeCallbacks(updateSeekBar);
    }
}