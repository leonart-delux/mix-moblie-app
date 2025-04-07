package hcmute.edu.vn.noicamheo.mediaplayer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
    private static final String TAG = "MediaPlayerActivity";
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private List<Song> filteredSongList = new ArrayList<>();
    private static final int REQUEST_CODE_PERMISSION = 123;
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
    private MediaPlayerService mediaPlayerService;
    private boolean serviceBound = false;

    private BroadcastReceiver playbackStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MediaPlayerService.ACTION_PLAYBACK_STATE_CHANGED.equals(intent.getAction())) {
                currentSongIndex = intent.getIntExtra(MediaPlayerService.EXTRA_CURRENT_SONG_INDEX, -1);
                boolean isPlaying = intent.getBooleanExtra(MediaPlayerService.EXTRA_IS_PLAYING, false);
                long currentPosition = intent.getLongExtra(MediaPlayerService.EXTRA_CURRENT_POSITION, 0);
                long duration = intent.getLongExtra(MediaPlayerService.EXTRA_DURATION, 0);
                boolean receivedIsRepeat = intent.getBooleanExtra(MediaPlayerService.EXTRA_IS_REPEAT, false);
                boolean receivedIsShuffle = intent.getBooleanExtra(MediaPlayerService.EXTRA_IS_SHUFFLE, false);

                if (isRepeat != receivedIsRepeat) {
                    isRepeat = receivedIsRepeat;
                    btnRepeat.setImageResource(isRepeat ? R.drawable.ic_repeat_on : R.drawable.ic_repeat);
                }

                if (isShuffle != receivedIsShuffle) {
                    isShuffle = receivedIsShuffle;
                    btnShuffle.setImageResource(isShuffle ? R.drawable.ic_shuffle_on : R.drawable.ic_shuffle);
                }

                if (currentSongIndex >= 0) {
                    Song currentSong;
                    if (isShuffle && mediaPlayerService != null) {
                        List<Song> activeList = mediaPlayerService.getActiveList();
                        if (currentSongIndex < activeList.size()) {
                            currentSong = activeList.get(currentSongIndex);
                        } else {
                            return;
                        }
                    } else if (currentSongIndex < songList.size()) {
                        currentSong = songList.get(currentSongIndex);
                    } else {
                        return;
                    }

                    tvSongTitle.setText(currentSong.getTitle());
                    tvArtistName.setText(currentSong.getArtist());
                }

                btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
                Log.d(TAG, "Updated play/pause button based on broadcast: isPlaying=" + isPlaying);

                seekBar.setMax((int) duration);
                seekBar.setProgress((int) currentPosition);
                tvCurrentTime.setText(formatTime((int) currentPosition));
                tvTotalTime.setText(formatTime((int) duration));

                if (isPlaying) {
                    handler.post(updateSeekBar);
                } else {
                    handler.removeCallbacks(updateSeekBar);
                }

                Log.d(TAG, "Received broadcast: currentSongIndex=" + currentSongIndex + ", isPlaying=" + isPlaying);
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
            mediaPlayerService = binder.getService();
            serviceBound = true;
            Log.d(TAG, "Service connected, setting song list");
            mediaPlayerService.setSongList(songList);
            updatePlayerListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            Log.d(TAG, "Service disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mediaplay);

        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(playbackStateReceiver,
                new IntentFilter(MediaPlayerService.ACTION_PLAYBACK_STATE_CHANGED));

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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSongs(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText);
                return true;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && serviceBound) {
                    mediaPlayerService.getPlayer().seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        if (serviceBound) {
            handler.post(updateSeekBar);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        handler.removeCallbacks(updateSeekBar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playbackStateReceiver);
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
            Log.d(TAG, "Service unbound");
        }
        handler.removeCallbacks(updateSeekBar);
        Log.d(TAG, "onDestroy called");
    }

    private void updatePlayerListener() {
        if (serviceBound) {
            ExoPlayer player = mediaPlayerService.getPlayer();
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY) {
                        seekBar.setMax((int) player.getDuration());
                        tvTotalTime.setText(formatTime((int) player.getDuration()));
                        handler.post(updateSeekBar);
                    }
                }
            });
            Log.d(TAG, "Player listener updated");
        } else {
            Log.w(TAG, "Service not bound, cannot update player listener");
        }
    }

    private void scanAndLoadSongs() {
        Log.d(TAG, "Scanning folder: " + MUSIC_FOLDER);
        MediaScannerConnection.scanFile(this, new String[]{MUSIC_FOLDER}, null,
                (path, uri) -> {
                    Log.d(TAG, "Scan completed for path: " + path + ", URI: " + uri);
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
            Log.w(TAG, "Permission denied");
        }
    }

    private void loadSongs() {
        Log.d(TAG, "Starting to load songs...");
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
            Log.d(TAG, "Cursor is not null, processing songs...");
            if (cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                do {
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    String path = cursor.getString(pathColumn);

                    Log.d(TAG, "Found song - Title: " + title + ", Artist: " + artist + ", Path: " + path);

                    if (path != null && path.endsWith(".mp3")) {
                        Song song = new Song(title, artist, path);
                        songList.add(song);
                        filteredSongList.add(song);
                    }
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "Cursor is empty, no songs found");
            }
            cursor.close();
        } else {
            Log.d(TAG, "Cursor is null, no songs found in MediaStore");
            Toast.makeText(this, "No MP3 files found", Toast.LENGTH_SHORT).show();
        }

        if (songAdapter != null) {
            songAdapter.notifyDataSetChanged();
        }
        if (serviceBound) {
            mediaPlayerService.setSongList(songList);
            Log.d(TAG, "Song list updated in service");
        }
    }

    private void toggleShuffle() {
        if (songList.isEmpty()) {
            Toast.makeText(this, "No songs available to shuffle", Toast.LENGTH_SHORT).show();
            return;
        }

        Song currentSong = null;
        if (serviceBound && mediaPlayerService.getPlayer() != null &&
                mediaPlayerService.getPlayer().isPlaying() && currentSongIndex >= 0 &&
                currentSongIndex < songList.size()) {
            currentSong = songList.get(currentSongIndex);
        }

        isShuffle = !isShuffle;
        if (serviceBound) {
            mediaPlayerService.setShuffle(isShuffle);
            if (isShuffle) {
                btnShuffle.setImageResource(R.drawable.ic_shuffle_on);
                Log.d(TAG, "Shuffle mode ON");
            } else {
                btnShuffle.setImageResource(R.drawable.ic_shuffle);
                Log.d(TAG, "Shuffle mode OFF");
            }

            if (currentSong != null) {
                List<Song> activeList = mediaPlayerService.getActiveList();
                for (int i = 0; i < activeList.size(); i++) {
                    if (activeList.get(i).getPath().equals(currentSong.getPath())) {
                        currentSongIndex = i;
                        break;
                    }
                }
            }
        } else {
            Log.w(TAG, "Service not bound, cannot toggle shuffle");
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
        Log.d(TAG, "Attempting to play song: " + song.getTitle());
        if (serviceBound) {
            try {
                // Find the song in the active list (which could be shuffled)
                List<Song> activeList = mediaPlayerService.getActiveList();
                int indexInActiveList = -1;

                for (int i = 0; i < activeList.size(); i++) {
                    if (activeList.get(i).getPath().equals(song.getPath())) {
                        indexInActiveList = i;
                        break;
                    }
                }

                if (indexInActiveList != -1) {
                    currentSongIndex = indexInActiveList;
                } else {
                    currentSongIndex = songList.indexOf(song);
                }

                Log.d(TAG, "Current song index: " + currentSongIndex);
                mediaPlayerService.playSong(currentSongIndex);
                Log.d(TAG, "Song played successfully: " + song.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "Error playing song: " + song.getTitle(), e);
                Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "Service not bound, cannot play song: " + song.getTitle());
            Toast.makeText(this, "Service not ready, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void playPreviousSong() {
        if (serviceBound) {
            try {
                mediaPlayerService.playPreviousSong();
                Log.d(TAG, "Previous song played");
            } catch (Exception e) {
                Log.e(TAG, "Error playing previous song", e);
            }
        } else {
            Log.w(TAG, "Service not bound, cannot play previous song");
        }
    }

    private void playNextSong() {
        if (serviceBound) {
            try {
                mediaPlayerService.playNextSong();
                Log.d(TAG, "Next song played");
            } catch (Exception e) {
                Log.e(TAG, "Error playing next song", e);
            }
        } else {
            Log.w(TAG, "Service not bound, cannot play next song");
        }
    }

    private void togglePlayPause() {
        if (serviceBound) {
            try {
                mediaPlayerService.togglePlayPause();
                Log.d(TAG, "Play/Pause toggled");
            } catch (Exception e) {
                Log.e(TAG, "Error toggling play/pause", e);
            }
        } else {
            Log.w(TAG, "Service not bound, cannot toggle play/pause");
        }
    }

    private void repeatSong() {
        isRepeat = !isRepeat;
        if (serviceBound) {
            mediaPlayerService.setRepeat(isRepeat);
            if (isRepeat) {
                btnRepeat.setImageResource(R.drawable.ic_repeat_on);
            } else {
                btnRepeat.setImageResource(R.drawable.ic_repeat);
            }
            Log.d(TAG, "Repeat mode set to: " + isRepeat);
        } else {
            Log.w(TAG, "Service not bound, cannot set repeat");
        }
    }

    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (serviceBound && mediaPlayerService.getPlayer() != null) {
                int currentPosition = (int) mediaPlayerService.getPlayer().getCurrentPosition();
                seekBar.setProgress(currentPosition);
                tvCurrentTime.setText(formatTime(currentPosition));
                handler.postDelayed(this, 500);
            }
        }
    };

    private String formatTime(int millis) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }
}