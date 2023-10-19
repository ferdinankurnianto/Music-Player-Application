package com.example.musicplayer;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class PlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    public static final String ACTION_PLAY = "com.example.musicplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.musicplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.musicplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.musicplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.musicplayer.ACTION_STOP";

    private AudioManager audioManager;

    private MusicRepository musicRepository;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;

    public MediaPlayer music;
    private String file_url, image_url, id, title, artist, category, singer, year, lyric="a";
    Bitmap image;

    private final Handler myHandler = new Handler();

    private static boolean isRunning;
    private static String _id;

    private Session session;

    private GetMusicFile mFile;

    public PlayerService() {
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static String getId() {
        return _id;
    }


    private final IBinder iBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        music = new MediaPlayer();
        session = new Session(getBaseContext());

        musicRepository = MusicRepository.getInstance();

        registerBecomingNoisyReceiver();
        register_playNewAudio();
        register_startAudio();
        register_pauseAudio();
        register_nextAudio();
        register_previousAudio();
        register_seekAudio();
        register_prefChanged();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //An audio file is passed to the service through putExtra();
            id = intent.getExtras().getString("id");

            musicRepository.setId(id);

            _id = id;

            isRunning = true;

            if(intent.getAction() == null) {
                if(mFile!=null)
                    mFile.cancel(true);
                mFile = (GetMusicFile) new GetMusicFile().execute(id);
            }

        } catch (NullPointerException e) {
            stopSelf();
        }

        handleIncomingActions(intent);

        if (!requestAudioFocus()) {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(audioManager != null)
            removeAudioFocus();
        if (music != null) {
            stopMedia();
            musicRepository.setPlayStatus(false);
            music.release();
        }
        try {
            unregisterReceiver(playNewAudio);
            unregisterReceiver(startAudio);
            unregisterReceiver(pauseAudio);
            unregisterReceiver(nextAudio);
            unregisterReceiver(previousAudio);
            unregisterReceiver(seekAudio);
            unregisterReceiver(prefChanged);
            removeNotification();
            musicRepository.setId("-1");
            isRunning = false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        removeNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(music!=null) {
            if (music.isPlaying()) {
                musicRepository.setProgress(music.getCurrentPosition());
                myHandler.postDelayed(Updateprogress, 100);
            }
        }
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        myHandler.removeCallbacks(Updateprogress);
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        if (music.isPlaying()) {
            musicRepository.setProgress(music.getCurrentPosition());
            session.checksession("id");
            myHandler.postDelayed(Updateprogress, 100);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.
        stopMedia();
        skipToNext();
    }

    //Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation.
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        musicRepository.setLoading(false);
        musicRepository.setDuration(music.getDuration());
        if(Boolean.TRUE.equals(musicRepository.getPlayStatus().getValue())){
            playMedia();
            buildNotification(PlaybackStatus.PLAYING);
        } else  {
            buildNotification(PlaybackStatus.PAUSED);
        }
        musicRepository.setProgress(music.getCurrentPosition());
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        //Invoked when the audio focus of the system is updated.
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (music == null) initMediaPlayer();
                else if (!music.isPlaying()) music.start();
                music.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (music.isPlaying()) music.stop();
                music.release();
                music = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (music.isPlaying()) music.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (music.isPlaying()) music.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private void removeAudioFocus() {
        audioManager.abandonAudioFocus(this);
    }

    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    private void initMediaPlayer() {
        Log.d("Music: ", "INIT");
        //Set up MediaPlayer event listeners
        music.setOnCompletionListener(this);
        music.setOnErrorListener(this);
        music.setOnPreparedListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        music.reset();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        music.setAudioAttributes(audioAttributes);
        try {
            // Set the data source to the mediaFile location
            music.setDataSource(file_url);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        music.prepareAsync();
        initPref();
    }

    public void initPref() {
        Integer pref = session.getPref();
        if (pref.equals(0)) {
            music.setLooping(false);
        } else if (pref.equals(1)) {
            music.setLooping(true);
        } else if (pref.equals(2)) {
            music.setLooping(false);
        }
    }

    private void playMedia() {
        if (!music.isPlaying()) {
            music.start();
            musicRepository.setProgress(music.getCurrentPosition());
            myHandler.removeCallbacks(Updateprogress);
            myHandler.postDelayed(Updateprogress, 100);
            musicRepository.setPlayStatus(true);

            buildNotification(PlaybackStatus.PLAYING);
        }
    }

    private void stopMedia() {
        if (music == null) return;
        if (music.isPlaying()) {
            music.stop();
            musicRepository.setProgress(0);
            myHandler.removeCallbacks(Updateprogress);
        }
    }

    private void pauseMedia() {
        if (music.isPlaying()) {
            music.pause();
            musicRepository.setPlayStatus(false);
            buildNotification(PlaybackStatus.PAUSED);
            myHandler.removeCallbacks(Updateprogress);
        }
    }

    private void skipToNext() {
        id = String.valueOf(Integer.parseInt(musicRepository.getId()) + 1);
        Integer pref = session.getPref();
        int idb = Integer.parseInt(id);
        String lastSong = session.getLastSong();
        int idl = Integer.parseInt(lastSong);

        if (pref.equals(2)) {
            Integer idRand = new Random().nextInt((idl - 1) + 1) + 1;
            while (idRand.equals(idb - 1)) {
                idRand = new Random().nextInt((idl - 1) + 1) + 1;
            }
            id = idRand.toString();
        } else if (idb>idl && pref.equals(0)) {
            id = "1";
        } else if (idb <= idl) {
            id = Integer.toString(idb);
        }
        if (Integer.parseInt(id) > Integer.parseInt(session.getLastSong()))
            id = "1";
        musicRepository.setId(id);

        stopMedia();
        //reset mediaPlayer
        music.reset();

        if(mFile!=null)
            mFile.cancel(true);
        mFile = (GetMusicFile) new GetMusicFile().execute(id);
    }

    private void skipToPrevious() {
        id = String.valueOf(Integer.parseInt(musicRepository.getId()) - 1);

        Integer pref = session.getPref();
        int idb = Integer.parseInt(id);
        String lastSong = session.getLastSong();
        int idl = Integer.parseInt(lastSong);

        if (pref.equals(2)) {
            Integer idRand = new Random().nextInt((idl - 1) + 1) + 1;
            while (idRand.equals(idb + 1)) {
                idRand = new Random().nextInt((idl - 1) + 1) + 1;
            }
            id = idRand.toString();
        } else if (idb > 0) {
            id = Integer.toString(idb);
        }
        if (Integer.parseInt(id) <= 0)
            id = session.getLastSong();

        musicRepository.setId(id);

        stopMedia();
        //reset mediaPlayer
        music.reset();

        if(mFile!=null)
            mFile.cancel(true);
        mFile = (GetMusicFile) new GetMusicFile().execute(id);
    }

    private void seekMedia(int seconds) {
        music.seekTo(seconds);
        musicRepository.setProgress(music.getCurrentPosition());
    }

    private void updateUiData() {
        musicRepository.setTitle(title);
        musicRepository.setCategory(category);
        musicRepository.setArtist(artist);
        musicRepository.setSinger(singer);
        musicRepository.setYear(year);
        musicRepository.setLyric(lyric);
    }

    private Runnable Updateprogress = new Runnable() {
        public void run() {
            musicRepository.setProgress(music.getCurrentPosition());
            myHandler.postDelayed(this, 100);
        }
    };

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
            id = musicRepository.getId();
            if (Integer.parseInt(id) == -1) {
                //index is in a valid range
                stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            musicRepository.setPlayStatus(false);
            music.reset();

            if(mFile!=null)
                mFile.cancel(true);
            mFile = (GetMusicFile) new GetMusicFile().execute(id);
        }
    };

    private BroadcastReceiver startAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playMedia();
        }
    };

    private BroadcastReceiver pauseAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
        }
    };

    private BroadcastReceiver nextAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            skipToNext();
        }
    };

    private BroadcastReceiver previousAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            skipToPrevious();
        }
    };

    private BroadcastReceiver seekAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Integer _seconds = intent.getIntExtra("seconds", 0);
            seekMedia(_seconds);
        }
    };

    private BroadcastReceiver prefChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initPref();
        }
    };

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(PlayerActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    private void register_startAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(PlayerActivity.Broadcast_START_AUDIO);
        registerReceiver(startAudio, filter);
    }

    private void register_pauseAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(PlayerActivity.Broadcast_PAUSE_AUDIO);
        registerReceiver(pauseAudio, filter);
    }

    private void register_nextAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(PlayerActivity.Broadcast_NEXT_AUDIO);
        registerReceiver(nextAudio, filter);
    }

    private void register_previousAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(PlayerActivity.Broadcast_PREVIOUS_AUDIO);
        registerReceiver(previousAudio, filter);
    }

    private void register_seekAudio() {
        IntentFilter filter = new IntentFilter(PlayerActivity.Broadcast_SEEK_AUDIO);
        registerReceiver(seekAudio, filter);
    }

    private void register_prefChanged() {
        IntentFilter filter = new IntentFilter(PlayerActivity.Broadcast_PREF_CHANGED);
        registerReceiver(prefChanged, filter);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "GKI_MP";
            NotificationChannel mChannel = new NotificationChannel(
                    channelId,
                    "Music Player",
                    NotificationManager.IMPORTANCE_HIGH
            );
            mChannel.setDescription("This is channel for musicplayer");
            mChannel.setSound(null,null);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(mChannel);
        }
    }

    private void buildNotification(PlaybackStatus playbackStatus) {
        createNotificationChannel();

        Intent playerIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        playerIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        PendingIntent playerPendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, playerIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_stat_name;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification);

        Bitmap largeIcon = musicRepository.getImage().getValue(); //replace with your own image

        notificationLayout.setImageViewBitmap(R.id.image, largeIcon);
        notificationLayout.setImageViewResource(R.id.action, notificationAction);
        notificationLayout.setTextViewText(R.id.title, musicRepository.getTitle().getValue());
        notificationLayout.setTextViewText(R.id.info, musicRepository.getArtist().getValue());
        notificationLayout.setOnClickPendingIntent(R.id.close, playbackAction(4));
        notificationLayout.setOnClickPendingIntent(R.id.prev, playbackAction(3));
        notificationLayout.setOnClickPendingIntent(R.id.next, playbackAction(2));

        if(Boolean.FALSE.equals(musicRepository.getLoading().getValue()))
            notificationLayout.setOnClickPendingIntent(R.id.action, play_pauseAction);

        // Create a new Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder notificationBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(this, "GKI_MP")
                            .setShowWhen(false)
                            // Set the Notification style
                            //made ongoing not work
                            // Set the Notification color
                            .setColor(getResources().getColor(R.color.black))
                            // Set the large and small icons
                            .setLargeIcon(largeIcon)
                            .setSmallIcon(android.R.drawable.stat_sys_headset)
                            // Set Notification content information
                            .setContentText(musicRepository.getArtist().getValue())
                            .setContentTitle(musicRepository.getTitle().getValue())
                            .setContentInfo(musicRepository.getTitle().getValue())
                            .setContentIntent(playerPendingIntent)
                            .setCustomContentView(notificationLayout)
                            .setOngoing(true).setAutoCancel(false);
            // Add playback actions
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
        } else {
            NotificationCompat.Builder notificationBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                            .setShowWhen(false)
                            // Set the Notification style
                            //made ongoing not work
                            // Set the Notification color
                            .setColor(getResources().getColor(R.color.black))
                            // Set the large and small icons
                            .setLargeIcon(largeIcon)
                            .setSmallIcon(android.R.drawable.stat_sys_headset)
                            // Set Notification content information
                            .setContentText(musicRepository.getArtist().getValue())
                            .setContentTitle(musicRepository.getTitle().getValue())
                            .setContentInfo(musicRepository.getTitle().getValue())
                            .setContentIntent(playerPendingIntent)
                            .setCustomContentView(notificationLayout)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setSound(null)
                            .setOngoing(true).setAutoCancel(false);
            // Add playback actions
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private void buildNotif() {
        if(Boolean.TRUE.equals(musicRepository.getPlayStatus().getValue()))
            buildNotification(PlaybackStatus.PLAYING);
        else
            buildNotification(PlaybackStatus.PAUSED);
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, PlayerService.class);
        playbackAction.putExtra("id", id);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction,
                        PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction,
                        PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction,
                        PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction,
                        PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            case 4:
                // Stop service
                playbackAction.setAction(ACTION_STOP);
                return PendingIntent.getService(this, actionNumber, playbackAction,
                        PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            playMedia();
            buildNotification(PlaybackStatus.PLAYING);
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            skipToNext();
            buildNotif();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            skipToPrevious();
            buildNotif();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            removeNotification();
            stopSelf();
        }
    }

    private class GetMusicFile extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        String link;
        URL url;
        public String TAGS = "MusicFile:";

        @Override
        protected String doInBackground(String... params) {
            try {
                musicRepository.postLoading(true);
                buildNotif();
                link = MyConfig.HOST + "/lagupujian/get";
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("id", params[0]);
                link = link+builder;
                String qry = builder.build().getEncodedQuery();
                url = new URL(link);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAGS, e.getMessage());
                return "exception";
            }

            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
            } catch (IOException e1) {
                e1.printStackTrace();
                return "exception";
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                int response_code = conn.getResponseCode();
                Log.d(TAGS, Integer.toString(response_code));
                if (response_code == HttpURLConnection.HTTP_OK) {
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        result.append(line);
                    return (result.toString());
                } else {
                    return ("unsuccessful");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAGS, e.getMessage());
                return "exception";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);

                title = jsonObject.getString("title");
                artist = jsonObject.getString("artist");
                category = jsonObject.getString("category");
                singer = jsonObject.getString("singer");
                year = jsonObject.getString("year");
                lyric = jsonObject.getString("lyric");
                lyric = lyric.replace("\\n", "\n");
                file_url = MyConfig.HOST + "/song/" + jsonObject.getString("file_url");
                image_url = MyConfig.HOST + "/img/" + jsonObject.getString("image_url");
                Picasso.get().load(image_url).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        image = bitmap;
                        musicRepository.setImage(image);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
                if (file_url != null && !file_url.equals("")) {
                    initMediaPlayer();
                }
                updateUiData();
                buildNotif();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}