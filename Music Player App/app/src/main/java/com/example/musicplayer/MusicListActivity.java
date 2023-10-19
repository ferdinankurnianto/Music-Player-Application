package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class MusicListActivity extends AppCompatActivity {
    public static final String Broadcast_START_AUDIO = "com.example.musicplayer.StartAudio";
    public static final String Broadcast_PAUSE_AUDIO = "com.example.musicplayer.PauseAudio";
    public static final String Broadcast_NEXT_AUDIO = "com.example.musicplayer.NextAudio";
    public static final String Broadcast_PREVIOUS_AUDIO = "com.example.musicplayer.PreviousAudio";

    private MusicViewModel musicViewModel;

    ArrayList<HashMap<String, String>> listItem;
    private Session session;

    private TextView titleView, infoView;
    private ImageView songImage, actionImage;
    private Boolean flag = false, playStatus = false;
    private String id;

    private String type;

    private GetMusicList mList;

    RecyclerView musicList;
    EditText search;
    CustomAdapterList adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        musicViewModel = new ViewModelProvider(this).get(MusicViewModel.class);

        session = new Session(getBaseContext());

        titleView = (TextView) findViewById(R.id.title);
        infoView = (TextView) findViewById(R.id.info);
        songImage = (ImageView) findViewById(R.id.image);
        actionImage = (ImageView) findViewById(R.id.action);

        type = session.getType();

        listItem = new ArrayList<>();
        adapter = new CustomAdapterList(listItem);

        musicList = findViewById(R.id.listRecycler);
        musicList.setLayoutManager(new LinearLayoutManager(MusicListActivity.this,
                LinearLayoutManager.VERTICAL, false));

        musicList.setAdapter(adapter);
        musicList.addItemDecoration(new DividerItemDecoration(musicList.getContext(), DividerItemDecoration.VERTICAL));

        new GetMusicList().execute("", type);

        search = findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mList != null)
                    mList.cancel(true);
                listItem.clear();
                mList = (GetMusicList) new GetMusicList().execute(s.toString(), type);
            }
        });

        initObservers();
    }

    @Override
    protected void onStart() {
        super.onStart();

        id = musicViewModel.getId();

        View footer = findViewById(R.id.footer);

        if(PlayerService.isRunning())
            footer.setVisibility(View.VISIBLE);
    }

    private class GetMusicList extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        String link;
        URL url;
        public String TAGS = "MusicFile:";

        @Override
        protected String doInBackground(String... params) {
            try {
                link = MyConfig.HOST + "/lagupujian/get?search=" + params[0] + "&type=" + params[1];
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
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    HashMap<String, String> list = new HashMap<>();
                    JSONObject object = jsonArray.getJSONObject(i);
                    list.put("id", object.getString("id"));
                    list.put("judul", object.getString("title"));
                    list.put("artist", object.getString("artist"));
                    list.put("singer", object.getString("singer"));
                    listItem.add(list);
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void goToPlayer(View view) {
        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
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
    }
}