package com.example.musicplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LyricFragment extends Fragment {
    TextView lirik;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        String lyric = getArguments().getString("lyric");
        final View rootView = inflater.inflate(R.layout.fragment_lyric, container, false);
        lirik = (TextView) rootView.findViewById(R.id.lyric);
        lirik.setText(lyric);
        return rootView;
    }
}