package hcmute.edu.vn.noicamheo.mediaplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    public boolean isRepeat() {
        return isRepeat;
    }

    public boolean isShuffle() {
        return isShuffle;
    }
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "MediaPlayerServiceChannel";

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            updateNotification();
            progressHandler.postDelayed(this, 1000);
        }
    };

    public static final String ACTION_PLAYBACK_STATE_CHANGED = "hcmute.edu.vn.noicamheo.ACTION_PLAYBACK_STATE_CHANGED";
    public static final String EXTRA_CURRENT_SONG_INDEX = "current_song_index";
    public static final String EXTRA_IS_PLAYING = "is_playing";
    public static final String EXTRA_CURRENT_POSITION = "current_position";
    public static final String EXTRA_DURATION = "duration";
    public static final String EXTRA_IS_REPEAT = "is_repeat";
    public static final String EXTRA_IS_SHUFFLE = "is_shuffle";

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
                    } else {
                        player.seekTo(0);
                        player.play();
                        sendPlaybackStateBroadcast();
                    }
                } else {
                    updateNotification();
                    sendPlaybackStateBroadcast();
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    progressHandler.post(progressRunnable);
                } else {
                    progressHandler.removeCallbacks(progressRunnable);
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
        List<Song> activeList = isShuffle ? shuffledSongList : songList;

        if (index >= 0 && index < activeList.size()) {
            try {
                currentSongIndex = index;
                Song currentSong = activeList.get(index);

                MediaItem mediaItem = new MediaItem.Builder()
                        .setUri(currentSong.getPath())
                        .setMediaMetadata(new MediaMetadata.Builder()
                                .setTitle(currentSong.getTitle())
                                .setArtist(currentSong.getArtist())
                                .build())
                        .build();

                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
                player.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);

                progressHandler.post(progressRunnable);

                startForeground(NOTIFICATION_ID, createNotification());
                sendPlaybackStateBroadcast();
                Log.d(TAG, "Playing song: " + currentSong.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "Error playing song at index " + index, e);
            }
        } else {
            Log.w(TAG, "Invalid index: " + index + ", activeList size: " + activeList.size());
        }
    }

    public void togglePlayPause() {
        try {
            if (player.isPlaying()) {
                player.pause();
                Log.d(TAG, "Player paused");
            } else {
                player.play();
                Log.d(TAG, "Player resumed");
            }
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
        sendPlaybackStateBroadcast();
    }

    public void setShuffle(boolean shuffle) {
        if (isShuffle != shuffle) {
            isShuffle = shuffle;
            Log.d(TAG, "Shuffle mode set to: " + isShuffle);

            if (isShuffle) {
                shuffledSongList = new ArrayList<>(songList);
                Collections.shuffle(shuffledSongList);

                if (currentSongIndex >= 0 && currentSongIndex < songList.size()) {
                    Song currentSong = songList.get(currentSongIndex);
                    int newIndex = shuffledSongList.indexOf(currentSong);
                    if (newIndex != 0 && newIndex >= 0) {
                        Collections.swap(shuffledSongList, 0, newIndex);
                    }
                    currentSongIndex = 0;
                }
            } else {
                if (currentSongIndex >= 0 && currentSongIndex < shuffledSongList.size()) {
                    Song currentSong = shuffledSongList.get(currentSongIndex);
                    currentSongIndex = songList.indexOf(currentSong);
                }
            }

            updateNotification();
            sendPlaybackStateBroadcast();
        }
    }

    public List<Song> getActiveList() {
        return isShuffle ? shuffledSongList : songList;
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

        String title = "";
        String artist = "";
        if (currentSongIndex >= 0) {
            List<Song> activeList = isShuffle ? shuffledSongList : songList;
            if (currentSongIndex < activeList.size()) {
                Song currentSong = activeList.get(currentSongIndex);
                title = currentSong.getTitle();
                artist = currentSong.getArtist();
            }
        }

        Intent notificationIntent = new Intent(this, MediaPlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent playPauseIntent = new Intent(this, MediaPlayerService.class).setAction("PLAY_PAUSE");
        PendingIntent playPausePendingIntent = PendingIntent.getService(this, 0, playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(this, MediaPlayerService.class).setAction("NEXT");
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent previousIntent = new Intent(this, MediaPlayerService.class).setAction("PREVIOUS");
        PendingIntent previousPendingIntent = PendingIntent.getService(this, 0, previousIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent shuffleIntent = new Intent(this, MediaPlayerService.class).setAction("SHUFFLE");
        PendingIntent shufflePendingIntent = PendingIntent.getService(this, 0, shuffleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent repeatIntent = new Intent(this, MediaPlayerService.class).setAction("REPEAT");
        PendingIntent repeatPendingIntent = PendingIntent.getService(this, 0, repeatIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int progress = 0;
        String currentTimeStr = "0:00";
        String totalTimeStr = "0:00";
        if (player.getDuration() > 0) {
            progress = (int) (player.getCurrentPosition() * 100 / player.getDuration());
            currentTimeStr = formatTime(player.getCurrentPosition());
            totalTimeStr = formatTime(player.getDuration());
        }

        RemoteViews collapsedView = new RemoteViews(getPackageName(), R.layout.notification_collapsed);
        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.notification_expanded);

        collapsedView.setTextViewText(R.id.notification_title, title);
        collapsedView.setTextViewText(R.id.notification_artist, artist);
        collapsedView.setProgressBar(R.id.notification_progress, 100, progress, false);
        collapsedView.setImageViewResource(R.id.notification_play_pause,
                player.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);

        collapsedView.setOnClickPendingIntent(R.id.notification_previous, previousPendingIntent);
        collapsedView.setOnClickPendingIntent(R.id.notification_play_pause, playPausePendingIntent);
        collapsedView.setOnClickPendingIntent(R.id.notification_next, nextPendingIntent);

        expandedView.setTextViewText(R.id.notification_title, title);
        expandedView.setTextViewText(R.id.notification_artist, artist);
        expandedView.setTextViewText(R.id.notification_current_time, currentTimeStr);
        expandedView.setTextViewText(R.id.notification_total_time, totalTimeStr);
        expandedView.setProgressBar(R.id.notification_progress, 100, progress, false);

        expandedView.setImageViewResource(R.id.notification_play_pause,
                player.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);

        expandedView.setImageViewResource(R.id.notification_shuffle,
                isShuffle ? R.drawable.ic_shuffle_on : R.drawable.ic_shuffle);
        expandedView.setImageViewResource(R.id.notification_repeat,
                isRepeat ? R.drawable.ic_repeat_on : R.drawable.ic_repeat);

        expandedView.setOnClickPendingIntent(R.id.notification_previous, previousPendingIntent);
        expandedView.setOnClickPendingIntent(R.id.notification_play_pause, playPausePendingIntent);
        expandedView.setOnClickPendingIntent(R.id.notification_next, nextPendingIntent);
        expandedView.setOnClickPendingIntent(R.id.notification_shuffle, shufflePendingIntent);
        expandedView.setOnClickPendingIntent(R.id.notification_repeat, repeatPendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_cd)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(player.isPlaying())
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle());

        Log.d(TAG, "Notification built with DecoratedCustomViewStyle");
        return builder.build();
    }

    private String formatTime(long millis) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
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
        intent.putExtra(EXTRA_IS_REPEAT, isRepeat);
        intent.putExtra(EXTRA_IS_SHUFFLE, isShuffle);

        List<Song> activeList = getActiveList();
        intent.putParcelableArrayListExtra("active_song_list", new ArrayList<>(activeList));

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "Broadcast sent: currentSongIndex=" + currentSongIndex +
                ", isPlaying=" + player.isPlaying() +
                ", isRepeat=" + isRepeat +
                ", isShuffle=" + isShuffle +
                ", activeListSize=" + activeList.size());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        progressHandler.removeCallbacks(progressRunnable);

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