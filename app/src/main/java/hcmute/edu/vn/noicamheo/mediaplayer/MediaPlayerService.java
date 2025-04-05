package hcmute.edu.vn.noicamheo.mediaplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.entity.Song;

public class MediaPlayerService extends MediaSessionService {
    private static final String TAG = "MediaPlayerService";
    private final IBinder binder = new MediaPlayerBinder();
    private ExoPlayer player;
    private MediaSession mediaSession;
    private List<Song> songList = new ArrayList<>();
    private List<Song> shuffledSongList = new ArrayList<>();
    private int currentSongIndex = -1;
    private boolean isRepeat = false;
    private boolean isShuffle = false;

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "MediaPlayerServiceChannel";

    // Broadcast actions
    public static final String ACTION_PLAYBACK_STATE_CHANGED = "hcmute.edu.vn.noicamheo.ACTION_PLAYBACK_STATE_CHANGED";
    public static final String EXTRA_CURRENT_SONG_INDEX = "current_song_index";
    public static final String EXTRA_IS_PLAYING = "is_playing";
    public static final String EXTRA_CURRENT_POSITION = "current_position";
    public static final String EXTRA_DURATION = "duration";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");
        player = new ExoPlayer.Builder(this).build();
        mediaSession = new MediaSession.Builder(this, player).build();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    if (!isRepeat) {
                        playNextSong();
                    }
                }
                updateNotification();
                sendPlaybackStateBroadcast();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Media Player Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String action = intent != null ? intent.getAction() : null;
        Log.d(TAG, "onStartCommand with action: " + action);
        if (action != null) {
            switch (action) {
                case "PLAY_PAUSE":
                    togglePlayPause();
                    break;
                case "NEXT":
                    playNextSong();
                    break;
                case "PREVIOUS":
                    playPreviousSong();
                    break;
                case "SHUFFLE":
                    toggleShuffle();
                    break;
                case "REPEAT":
                    toggleRepeat();
                    break;
            }
        }
        return START_STICKY;
    }

    public class MediaPlayerBinder extends Binder {
        MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bound");
        IBinder superBinder = super.onBind(intent);
        return superBinder != null ? superBinder : binder;
    }

    @NonNull
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        Log.d(TAG, "onGetSession called");
        return mediaSession;
    }

    public void setSongList(List<Song> songs) {
        this.songList = songs;
        this.shuffledSongList = new ArrayList<>(songs);
        Log.d(TAG, "Song list set with size: " + songs.size());
        sendPlaybackStateBroadcast();
    }

    public void playSong(int index) {
        Log.d(TAG, "Attempting to play song at index: " + index);
        if (index >= 0 && index < (isShuffle ? shuffledSongList : songList).size()) {
            try {
                currentSongIndex = index;
                List<Song> activeList = isShuffle ? shuffledSongList : songList;
                MediaItem mediaItem = MediaItem.fromUri(activeList.get(index).getPath());
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
                player.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
                startForeground(NOTIFICATION_ID, createNotification());
                sendPlaybackStateBroadcast(); // Gửi broadcast
                Log.d(TAG, "Playing song: " + activeList.get(index).getTitle());
            } catch (Exception e) {
                Log.e(TAG, "Error playing song at index " + index, e);
            }
        } else {
            Log.w(TAG, "Invalid index: " + index + ", songList size: " + songList.size());
        }
    }

    public void togglePlayPause() {
        try {
            if (player.isPlaying()) {
                player.pause();
                Log.d(TAG, "Player paused");
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.notify(NOTIFICATION_ID, createNotification());
            } else {
                player.play();
                Log.d(TAG, "Player resumed");
                startForeground(NOTIFICATION_ID, createNotification());
            }
            sendPlaybackStateBroadcast(); // Gửi broadcast
        } catch (Exception e) {
            Log.e(TAG, "Error toggling play/pause", e);
        }
    }

    public void playNextSong() {
        Log.d(TAG, "Playing next song");
        List<Song> activeList = isShuffle ? shuffledSongList : songList;
        if (activeList.isEmpty()) {
            Log.w(TAG, "Song list is empty");
            return;
        }
        if (currentSongIndex >= activeList.size() - 1) {
            currentSongIndex = 0;
        } else {
            currentSongIndex++;
        }
        playSong(currentSongIndex);
    }

    public void playPreviousSong() {
        Log.d(TAG, "Playing previous song");
        List<Song> activeList = isShuffle ? shuffledSongList : songList;
        if (activeList.isEmpty()) {
            Log.w(TAG, "Song list is empty");
            return;
        }
        if (currentSongIndex <= 0) {
            currentSongIndex = activeList.size() - 1;
        } else {
            currentSongIndex--;
        }
        playSong(currentSongIndex);
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
        player.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        Log.d(TAG, "Repeat mode set to: " + isRepeat);
        updateNotification();
        sendPlaybackStateBroadcast(); // Gửi broadcast
    }

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
        Log.d(TAG, "Shuffle mode set to: " + isShuffle);
        if (isShuffle) {
            shuffledSongList = new ArrayList<>(songList);
            Collections.shuffle(shuffledSongList);
        }
        updateNotification();
        sendPlaybackStateBroadcast(); // Gửi broadcast
    }

    private void toggleShuffle() {
        setShuffle(!isShuffle);
    }

    private void toggleRepeat() {
        setRepeat(!isRepeat);
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    @OptIn(markerClass = UnstableApi.class)
    private Notification createNotification() {
        Log.d(TAG, "Creating notification for song index: " + currentSongIndex);
        Intent notificationIntent = new Intent(this, MediaPlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent playPauseIntent = new Intent(this, MediaPlayerService.class).setAction("PLAY_PAUSE");
        PendingIntent playPausePendingIntent = PendingIntent.getService(this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(this, MediaPlayerService.class).setAction("NEXT");
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent previousIntent = new Intent(this, MediaPlayerService.class).setAction("PREVIOUS");
        PendingIntent previousPendingIntent = PendingIntent.getService(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent shuffleIntent = new Intent(this, MediaPlayerService.class).setAction("SHUFFLE");
        PendingIntent shufflePendingIntent = PendingIntent.getService(this, 0, shuffleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent repeatIntent = new Intent(this, MediaPlayerService.class).setAction("REPEAT");
        PendingIntent repeatPendingIntent = PendingIntent.getService(this, 0, repeatIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        List<Song> activeList = isShuffle ? shuffledSongList : songList;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_cd)
                .setContentTitle(activeList.get(currentSongIndex).getTitle())
                .setContentText(activeList.get(currentSongIndex).getArtist())
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_shuffle, "Shuffle", shufflePendingIntent)
                .addAction(R.drawable.ic_previous, "Previous", previousPendingIntent)
                .addAction(player.getPlaybackState() == Player.STATE_IDLE ? R.drawable.ic_play : (player.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play), "Play/Pause", playPausePendingIntent)
                .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
                .addAction(R.drawable.ic_repeat, "Repeat", repeatPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionCompatToken())
                        .setShowActionsInCompactView(0, 1, 2, 3, 4))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(player.isPlaying());

        return builder.build();
    }

    private void updateNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, createNotification());
    }

    private void sendPlaybackStateBroadcast() {
        Intent intent = new Intent(ACTION_PLAYBACK_STATE_CHANGED);
        intent.putExtra(EXTRA_CURRENT_SONG_INDEX, currentSongIndex);
        intent.putExtra(EXTRA_IS_PLAYING, player.isPlaying());
        intent.putExtra(EXTRA_CURRENT_POSITION, player.getCurrentPosition());
        intent.putExtra(EXTRA_DURATION, player.getDuration());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "Broadcast sent: currentSongIndex=" + currentSongIndex + ", isPlaying=" + player.isPlaying());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            Log.d(TAG, "Player released");
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
        stopForeground(true);
        Log.d(TAG, "Service destroyed");
    }
}