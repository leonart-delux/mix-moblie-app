package hcmute.edu.vn.noicamheo.mediaplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.adapter.SongAdapter;
import hcmute.edu.vn.noicamheo.entity.Song;

public class MediaPlayerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private List<Song> filteredSongList = new ArrayList<>();
    private static final int REQUEST_CODE_PERMISSION = 123;
    private ExoPlayer player;
    private TextView tvSongTitle, tvArtistName;
    private ImageButton btnPlayPause, btnPrevious, btnNext, btnRepeat, btnShuffle;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvTotalTime;
    private Handler handler = new Handler();
    private int currentSongIndex = -1;
    private boolean isRepeat = false;
    private boolean isShuffle = false;
    private SearchView searchView;
    private static final String MUSIC_FOLDER = "/storage/emulated/0/Music/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MediaPlayer", "onCreate called");
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
        btnRepeat = findViewById(R.id.btn_repeat);
        btnShuffle = findViewById(R.id.btn_shuffle);
        seekBar = findViewById(R.id.seek_bar);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        searchView = findViewById(R.id.sv_media);
        searchView.setQueryHint("Search");

        btnRepeat.setImageResource(R.drawable.ic_repeat);
        btnShuffle.setImageResource(R.drawable.ic_shuffle);

        player = new ExoPlayer.Builder(this).build();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    seekBar.setMax((int) player.getDuration());
                    tvTotalTime.setText(formatTime((int) player.getDuration()));
                } else if (state == Player.STATE_ENDED) {
                    if (!isRepeat) {
                        playNextSong();
                    }
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
            scanAndLoadSongs();
        } else {
            requestPermission();
        }

        songAdapter = new SongAdapter(this, filteredSongList, this::playSong);
        recyclerView.setAdapter(songAdapter);
        filteredSongList.addAll(songList);

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnPrevious.setOnClickListener(v -> playPreviousSong());
        btnNext.setOnClickListener(v -> playNextSong());
        btnRepeat.setOnClickListener(v -> repeatSong());
        btnShuffle.setOnClickListener(v -> toggleShuffle());
    }

    private void scanAndLoadSongs() {
        Log.d("MediaPlayer", "Scanning folder: " + MUSIC_FOLDER);
        MediaScannerConnection.scanFile(this, new String[]{MUSIC_FOLDER}, null,
                (path, uri) -> {
                    Log.d("MediaScanner", "Scan completed for path: " + path + ", URI: " + uri);
                    handler.postDelayed(this::loadSongs, 500);
                });
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
            scanAndLoadSongs();
        } else {
            Toast.makeText(this, "Permission denied, cannot load songs", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSongs() {
        Log.d("MediaPlayer", "Starting to load songs...");
        songList.clear();
        filteredSongList.clear();
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

        if (cursor != null) {
            Log.d("MediaPlayer", "Cursor is not null, processing songs...");
            if (cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                do {
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    String path = cursor.getString(pathColumn);

                    Log.d("SongPath", "Found path: " + path);

                    if (path != null && path.endsWith(".mp3")) {
                        Song song = new Song(title, artist, path);
                        songList.add(song);
                        filteredSongList.add(song);
                    }
                } while (cursor.moveToNext());
            } else {
                Log.d("MediaPlayer", "Cursor is empty, no songs found");
            }
            cursor.close();
        } else {
            Log.d("MediaPlayer", "Cursor is null, no songs found in MediaStore");
            Toast.makeText(this, "No MP3 files found", Toast.LENGTH_SHORT).show();
        }

        if (songAdapter != null) {
            songAdapter.notifyDataSetChanged();
        }
    }

    private void toggleShuffle() {
        if (songList.isEmpty()) {
            Toast.makeText(this, "No songs available to shuffle", Toast.LENGTH_SHORT).show();
            return;
        }

        isShuffle = !isShuffle;
        if (isShuffle) {
            Collections.shuffle(songList);
            btnShuffle.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
            Log.d("MediaPlayer", "Shuffle mode ON");
        } else {
            loadSongs();
            btnShuffle.clearColorFilter();
            Log.d("MediaPlayer", "Shuffle mode OFF");
        }

        if (player.isPlaying()) {
            Song currentSong = filteredSongList.get(currentSongIndex);
            currentSongIndex = songList.indexOf(currentSong);
        }
    }

    private void filterSongs(String query) {
        filteredSongList.clear();
        if (query.isEmpty()) {
            filteredSongList.addAll(songList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Song song : songList) {
                if (song.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                        song.getArtist().toLowerCase().contains(lowerCaseQuery)) {
                    filteredSongList.add(song);
                }
            }
        }
        songAdapter.notifyDataSetChanged();
    }

    private void playSong(Song song) {
        currentSongIndex = songList.indexOf(song);

        tvSongTitle.setText(song.getTitle());
        tvArtistName.setText(song.getArtist());

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(song.getPath()));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        player.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);

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

    private void repeatSong() {
        isRepeat = !isRepeat;
        if (isRepeat) {
            btnRepeat.setImageResource(R.drawable.ic_repeat_one);
            player.setRepeatMode(Player.REPEAT_MODE_ONE);
        } else {
            btnRepeat.setImageResource(R.drawable.ic_repeat);
            player.setRepeatMode(Player.REPEAT_MODE_OFF);
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