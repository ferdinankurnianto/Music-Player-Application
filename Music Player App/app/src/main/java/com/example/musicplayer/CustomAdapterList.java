package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomAdapterList extends RecyclerView.Adapter<CustomAdapterList.ViewHolder>{
    private Context context;
    private ArrayList<HashMap<String, String>> localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView id;
        private final TextView judul;
        private final TextView artist;
        private final TextView singer;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            id = (TextView) view.findViewById(R.id.id);
            judul = (TextView) view.findViewById(R.id.judul);
            artist = (TextView) view.findViewById(R.id.artist);
            singer = (TextView) view.findViewById(R.id.singer);
        }
    }

    public CustomAdapterList(ArrayList<HashMap<String, String>> dataSet) {
        localDataSet = dataSet;
    }

    @NonNull
    @Override
    public CustomAdapterList.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item, viewGroup, false);
        context = viewGroup.getContext();
        return new CustomAdapterList.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomAdapterList.ViewHolder viewHolder, int position) {
        viewHolder.id.setText(localDataSet.get(position).get("id"));
        viewHolder.judul.setText(localDataSet.get(position).get("judul"));
        viewHolder.artist.setText(localDataSet.get(position).get("artist"));
        viewHolder.singer.setText(localDataSet.get(position).get("singer"));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context.getApplicationContext(), PlayerActivity.class);
                intent.putExtra("id", String.valueOf(localDataSet
                        .get(viewHolder.getAdapterPosition()).get("id")));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
