package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private Context context;
    private ArrayList<HashMap<String, String>> localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView id;
        private final ImageView image;
        private final TextView kategori;
        private final TextView judul;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            id = (TextView) view.findViewById(R.id.id);
            image = (ImageView) view.findViewById(R.id.image);
            kategori = (TextView) view.findViewById(R.id.kategori);
            judul = (TextView) view.findViewById(R.id.judul);
        }
    }

    public CustomAdapter(ArrayList<HashMap<String, String>> dataSet) {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_item, viewGroup, false);
        context = viewGroup.getContext();
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.id.setText(localDataSet.get(position).get("id"));
        Picasso.get().load(localDataSet.get(position).get("image")).fit().into(viewHolder.image);
        viewHolder.kategori.setText(localDataSet.get(position).get("kategori"));
        viewHolder.judul.setText(localDataSet.get(position).get("judul"));
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

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
