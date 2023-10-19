package com.example.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.DatabaseReference;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.musicplayer.PlayNewAudio";
    public static final String Broadcast_START_AUDIO = "com.example.musicplayer.StartAudio";
    public static final String Broadcast_PAUSE_AUDIO = "com.example.musicplayer.PauseAudio";
    public static final String Broadcast_NEXT_AUDIO = "com.example.musicplayer.NextAudio";
    public static final String Broadcast_PREVIOUS_AUDIO = "com.example.musicplayer.PreviousAudio";
    public static final String Broadcast_SEEK_AUDIO = "com.example.musicplayer.SeekAudio";
    public static final String Broadcast_PREF_CHANGED = "com.example.musicplayer.PrefChanged";

    private MediaPlayer music;
    private SeekBar pb;
    private TextView start, end, titleView, artistView, categoryView, singerView, yearView;
    private ImageView play, songImage, preference;
    private double startTime = 0;
    private Handler myHandler = new Handler();
    private Boolean flag = false, playStatus = false;
    private String id, title, artist, category, singer, year, file_url, image_url, lastSong, _id;
    private String lyric = "";
    private Integer pref;
    MyConfig config;
    private Session session;
    ProgressBar progressBar;

    private PlayerService player;
    boolean serviceBound = false, serviceStarted = false;

    private MusicViewModel musicViewModel;

    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        session = new Session(getBaseContext());

        pref = session.getPref();

        _id = getIntent().getStringExtra("id");

        musicViewModel = new ViewModelProvider(this).get(MusicViewModel.class);

        id = musicViewModel.getId();

        pb = findViewById(R.id.musicBar);
        pb.setClickable(true);
        start = (TextView) findViewById(R.id.currentTime);
        end = (TextView) findViewById(R.id.maxTime);
        preference = (ImageView) findViewById(R.id.preference);
        play = (ImageView) findViewById(R.id.play);
        titleView = (TextView) findViewById(R.id.title);
        artistView = (TextView) findViewById(R.id.artist);
        categoryView = (TextView) findViewById(R.id.category);
        singerView = (TextView) findViewById(R.id.singer);
        yearView = (TextView) findViewById(R.id.year);
        songImage = (ImageView) findViewById(R.id.songImage);
        lastSong = session.getLastSong();

        initObservers();
        setProgressbar();
        initPref();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Objects.equals(_id, id)) {
            if (!Objects.equals(id, "-1") && !Objects.equals(_id, id) && _id!=null)
                playNewAudio();
            else if (_id == null)
                playAudio(_id);
            else
                playAudio(_id);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceBound) {
            try {
                unbindService(serviceConnection);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        savedInstanceState.putBoolean("ServiceStart", serviceStarted);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
        serviceStarted = savedInstanceState.getBoolean("ServiceStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void playAudio(String id) {
        //Check is service is active
        Intent playerIntent = new Intent(this, PlayerService.class);
        playerIntent.putExtra("id", id);
        if (!PlayerService.isRunning()) {
            startService(playerIntent);
            serviceStarted = true;
        }
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void playNewAudio() {
        musicViewModel.setId(_id);
        Intent playerIntent = new Intent(this, PlayerService.class);
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
        sendBroadcast(broadcastIntent);
    }

    private void setProgressbar() {
        LinearLayout layout = findViewById(R.id.root);
        progressBar = new ProgressBar(PlayerActivity.this, null,
                android.R.attr.progressBarStyleHorizontal);
        progressBar.getIndeterminateDrawable().setColorFilter(
                Color.parseColor("Purple"), android.graphics.PorterDuff.Mode.SRC_IN);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , 100);
        params.gravity = Gravity.CENTER;
        layout.addView(progressBar, 1, params);
        progressBar.setIndeterminate(true);
    }

    public void initPref() {
        if (pref.equals(0)) {
            preference.setImageResource(R.drawable.ic_loop);
        } else if (pref.equals(1)) {
            preference.setImageResource(R.drawable.ic_loop_cur);
        } else if (pref.equals(2)) {
            preference.setImageResource(R.drawable.ic_shuffle);
        }
    }


    public void prefHandler(View view) {
        if (pref.equals(0)) {
            preference.setImageResource(R.drawable.ic_loop_cur);
            pref = 1;
        } else if (pref.equals(1)) {
            preference.setImageResource(R.drawable.ic_shuffle);
            pref = 2;
        } else if (pref.equals(2)) {
            preference.setImageResource(R.drawable.ic_loop);
            pref = 0;
        }
        session.setPref(pref);
        Intent broadcastIntent = new Intent(Broadcast_PREF_CHANGED);
        sendBroadcast(broadcastIntent);
    }

    public void musicPlayHandler(View view) {
        if (!flag) {
            musicplay(view);
            flag = true;
        } else {
            musicpause(view);
            flag = false;
        }
    }


    public void musicplay(View v) {
        play.setImageResource(R.drawable.ic_pause);
        Intent broadcastIntent = new Intent(Broadcast_START_AUDIO);
        sendBroadcast(broadcastIntent);
    }

    // Pausing the music
    public void musicpause(View v) {
        play.setImageResource(R.drawable.ic_stat_name);
        Intent broadcastIntent = new Intent(Broadcast_PAUSE_AUDIO);
        sendBroadcast(broadcastIntent);
    }

    public void next(View v) {
        int idb = Integer.parseInt(id);
        int idl = Integer.parseInt(lastSong);
        if (idb <= idl) {
            Intent broadcastIntent = new Intent(Broadcast_NEXT_AUDIO);
            sendBroadcast(broadcastIntent);
        }
        progressBar.setVisibility(View.VISIBLE);
        play.setClickable(false);
    }

    public void back(View v) {
        int idb = Integer.parseInt(id);
        idb = idb - 1;
        if (idb >= 0) {
            Intent broadcastIntent = new Intent(Broadcast_PREVIOUS_AUDIO);
            sendBroadcast(broadcastIntent);
        }
        progressBar.setVisibility(View.VISIBLE);
        play.setClickable(false);
    }

    private void initObservers() {
        musicViewModel.getTitle().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                titleView.setText(s);
            }
        });

        musicViewModel.getCategory().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                categoryView.setText(s);
            }
        });

        musicViewModel.getArtist().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                artistView.setText(s);
            }
        });

        musicViewModel.getSinger().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                singerView.setText(s);
            }
        });

        musicViewModel.getYear().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                yearView.setText(s);
            }
        });

        musicViewModel.getLyric().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Bundle bundle = new Bundle();
                bundle.putString("lyric", s);
                LyricFragment lirik = new LyricFragment();
                lirik.setArguments(bundle);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.lyricPlaceholder, lirik);

                ft.commit();
            }
        });

        musicViewModel.getImage().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap s) {
                ImageViewAnimatedChange(PlayerActivity.this, songImage, s);
            }
        });

        Observer<Integer> setSeekBar = new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                start.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes((long) s),
                        TimeUnit.MILLISECONDS.toSeconds((long) s) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                        toMinutes((long) s)))
                );
                pb.setProgress(s);
            }
        };

        musicViewModel.getDuration().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                pb.setMax(s);
                end.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes((long) s),
                        TimeUnit.MILLISECONDS.toSeconds((long) s) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                        toMinutes((long) s)))
                );
            }
        });

        musicViewModel.getProgress().observe(this, setSeekBar);

        musicViewModel.getPlayStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean s) {
                playStatus = s;
                if (playStatus) {
                    play.setImageResource(R.drawable.ic_pause);
                    flag = true;
                } else {
                    play.setImageResource(R.drawable.ic_stat_name);
                    flag = false;
                }
            }
        });

        musicViewModel.getLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean s) {
                if (s) {
                    progressBar.setVisibility(View.VISIBLE);
                    play.setClickable(false);
                } else {
                    progressBar.setVisibility(View.GONE);
                    play.setClickable(true);
                }
            }
        });

        pb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // handle progress change
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                musicViewModel.getProgress().removeObserver(setSeekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int seconds = seekBar.getProgress();
                Intent broadcastIntent = new Intent(Broadcast_SEEK_AUDIO);
                broadcastIntent.putExtra("seconds", seconds);
                sendBroadcast(broadcastIntent);
                musicViewModel.getProgress().observe(PlayerActivity.this, setSeekBar);
            }
        });
    }

    public static void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.slide_out_right);
        final Animation anim_in = AnimationUtils.loadAnimation(c, android.R.anim.slide_in_left);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }
}