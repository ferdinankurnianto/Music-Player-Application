package com.example.musicplayer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    public static final String Broadcast_START_AUDIO = "com.example.musicplayer.StartAudio";
    public static final String Broadcast_PAUSE_AUDIO = "com.example.musicplayer.PauseAudio";
    public static final String Broadcast_NEXT_AUDIO = "com.example.musicplayer.NextAudio";
    public static final String Broadcast_PREVIOUS_AUDIO = "com.example.musicplayer.PreviousAudio";

    private MusicViewModel musicViewModel;

    private TextView titleView, infoView;
    private ImageView songImage, actionImage;
    private View footer;
    private Boolean flag = false, playStatus = false;
    private String id;

    ArrayList<HashMap<String, String>> listPlayer;
    ArrayList<HashMap<String, String>> listPlayer1;
    ArrayList<HashMap<String, String>> listPlayer2;
    ArrayList<HashMap<String, String>> listPlayer3;
    ArrayList<HashMap<String, String>> listPlayer4;
    private Session session;
    private int count = 0;

    RecyclerView lagu;
    RecyclerView pkj;
    RecyclerView nkb;
    RecyclerView lpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicViewModel = new ViewModelProvider(this).get(MusicViewModel.class);

        session = new Session(getBaseContext());

        listPlayer = new ArrayList<>();
        listPlayer1 = new ArrayList<>();
        listPlayer2 = new ArrayList<>();
        listPlayer3 = new ArrayList<>();
        listPlayer4 = new ArrayList<>();

        titleView = (TextView) findViewById(R.id.title);
        infoView = (TextView) findViewById(R.id.info);
        songImage = (ImageView) findViewById(R.id.image);
        actionImage = (ImageView) findViewById(R.id.action);

        lagu = findViewById(R.id.laguRecycler);
        lagu.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                LinearLayoutManager.HORIZONTAL, false));
        pkj = findViewById(R.id.pkjRecycler);
        pkj.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                LinearLayoutManager.HORIZONTAL, false));
        nkb = findViewById(R.id.nkbRecycler);
        nkb.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                LinearLayoutManager.HORIZONTAL, false));
        lpl = findViewById(R.id.lplRecycler);
        lpl.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                LinearLayoutManager.HORIZONTAL, false));

        new GetMusicInfo().execute("kj");

        new GetMusicInfo().execute("pkj");

        new GetMusicInfo().execute("nkb");
        new GetMusicInfo().execute("lpl");

        initObservers();

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                return;
            ActivityResultLauncher<String> launcher = this.registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(), isGranted ->{
                    }
            );
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        id = musicViewModel.getId();

        footer = findViewById(R.id.footer);

        if(PlayerService.isRunning())
            footer.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        session.setId("-1");
    }

    public void toMusicList(View view) {
        session.setType("");
        Intent intent = new Intent(this.getApplicationContext(), MusicListActivity.class);
        startActivity(intent);
    }

    public void toKjList(View view) {
        session.setType("kj");
        Intent intent = new Intent(this.getApplicationContext(), MusicListActivity.class);
        startActivity(intent);
    }

    public void toPkjList(View view) {
        session.setType("pkj");
        Intent intent = new Intent(this.getApplicationContext(), MusicListActivity.class);
        startActivity(intent);
    }

    public void toNkbList(View view) {
        session.setType("nkb");
        Intent intent = new Intent(this.getApplicationContext(), MusicListActivity.class);
        startActivity(intent);
    }

    public void toLplList(View view) {
        session.setType("lpl");
        Intent intent = new Intent(this.getApplicationContext(), MusicListActivity.class);
        startActivity(intent);
    }

    public void goToPlayer(View view) {
        Intent intent = new Intent(this.getApplicationContext(), PlayerActivity.class);
        startActivity(intent);
    }

    private class GetMusicInfo extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        String link, link2;
        URL url, url2;
        public String TAGS = "MusicFile:";

        @Override
        protected String doInBackground(String... params) {
            try {
                link = MyConfig.HOST + "/lagupujian/get?limit=10&type="+params[0];
                link2 = MyConfig.HOST +"/lagupujian/last";
                url = new URL(link);
                url2 = new URL(link2);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAGS, e.getMessage());
                return "exception";
            }

            try {
                conn = (HttpURLConnection) url2.openConnection();
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
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    JSONArray array = new JSONArray(result.toString());
                    JSONObject object = array.getJSONObject(0);
                    session.setLastSong(object.getString("id"));
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAGS, e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
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
                JSONArray jsonArray = new JSONArray(result);

                for (int i = 0; i < jsonArray.length(); i++) {
                    HashMap<String, String> list = new HashMap<>();
                    JSONObject object = jsonArray.getJSONObject(i);
                    list.put("id", object.getString("id"));
                    list.put("image", MyConfig.HOST + "/img/" + object.getString("image_url"));
                    list.put("kategori", "Lagu Pujian");
                    list.put("judul", object.getString("title"));
                    listPlayer.add(list);
                }
                if(count == 0) {
                    count++;
                    listPlayer1.addAll(listPlayer);
                    listPlayer.clear();
                    lagu.setAdapter(new CustomAdapter(listPlayer1));
                } else if(count == 1) {
                    count++;
                    listPlayer2.addAll(listPlayer);
                    listPlayer.clear();
                    pkj.setAdapter(new CustomAdapter(listPlayer2));
                } else if(count == 2) {
                    count++;
                    listPlayer3.addAll(listPlayer);
                    listPlayer.clear();
                    nkb.setAdapter(new CustomAdapter(listPlayer3));
                } else {
                    count++;
                    listPlayer4.addAll(listPlayer);
                    listPlayer.clear();
                    lpl.setAdapter(new CustomAdapter(listPlayer4));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
        actionImage.setImageResource(R.drawable.ic_pause);
        Intent broadcastIntent = new Intent(Broadcast_START_AUDIO);
        sendBroadcast(broadcastIntent);
    }

    // Pausing the music
    public void musicpause(View v) {
        actionImage.setImageResource(R.drawable.ic_stat_name);
        Intent broadcastIntent = new Intent(Broadcast_PAUSE_AUDIO);
        sendBroadcast(broadcastIntent);
    }

    public void next(View v) {
        int idb = Integer.parseInt(id);
        int idl = Integer.parseInt(session.getLastSong());
        if (idb <= idl) {
            Intent broadcastIntent = new Intent(Broadcast_NEXT_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    public void back(View v) {
        int idb = Integer.parseInt(id);
        idb = idb - 1;
        if (idb >= 0) {
            Intent broadcastIntent = new Intent(Broadcast_PREVIOUS_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }


    private void initObservers() {
        musicViewModel.getTitle().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                titleView.setText(s);
            }
        });

        musicViewModel.getArtist().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                infoView.setText(s);
            }
        });

        musicViewModel.getImage().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap s) {
                songImage.setImageBitmap(s);
            }
        });

        musicViewModel.getPlayStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean s) {
                playStatus = s;
                if (playStatus) {
                    actionImage.setImageResource(R.drawable.ic_pause);
                    flag = true;
                } else {
                    actionImage.setImageResource(R.drawable.ic_stat_name);
                    flag = false;
                }
            }
        });

        musicViewModel.getLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean s) {
                actionImage.setClickable(!s);
                footer.setClickable(!s);
            }
        });
    }
}