package com.example.justnotes;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private final ArrayList<Note> localDataSet;
    private final Context parentContext;
    private final FavoritesAdapter.favoritesInterface listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView itemTitle;
        private final TextView itemDescription;
        private final ImageButton deleteButton;
        private final ImageButton editButton;
        private final ImageButton favButton;
        private final TextView itemTags;

        public ViewHolder(View view) {
            super(view);
            itemTitle = view.findViewById(R.id.item_title);
            itemDescription = view.findViewById(R.id.item_description);
            deleteButton = view.findViewById(R.id.delete_button);
            editButton = view.findViewById(R.id.edit_button);
            favButton = view.findViewById(R.id.fav_button);
            itemTags = view.findViewById(R.id.item_tags);
        }

        public TextView getItemTitle() {
            return itemTitle;
        }
        public TextView getItemDescription() {
            return itemDescription;
        }
        public ImageButton getDeleteButton() {
            return deleteButton;
        }
        public ImageButton getEditButton() {
            return editButton;
        }
        public ImageButton getFavButton() {
            return favButton;
        }
        public TextView getItemTags() {
            return itemTags;
        }

    }

    public FavoritesAdapter(Context current, ArrayList<Note> dataSet, favoritesInterface listener) {
        localDataSet = dataSet;
        parentContext = current;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);

        return new CustomAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapter.ViewHolder holder, int position) {
        holder.getItemTitle().setText(localDataSet.get(position).getTitle());
        holder.getItemDescription().setText(localDataSet.get(position).getDescription());

        List<String> tags = localDataSet.get(position).getTags();
        String itemTags = "";
        for(int i = 0; i < tags.size(); i++) {
            if(i == tags.size() - 1)
                itemTags += tags.get(i);
            else
                itemTags = tags.get(i) + ", ";
        }
        holder.getItemTags().setText(itemTags);

        ImageButton deleteButton = holder.getDeleteButton();
        ImageButton editButton = holder.getEditButton();
        ImageButton favButton = holder.getFavButton();

        boolean favorite = localDataSet.get(position).isFavorite();
        if(favorite)
            favButton.setImageResource(R.drawable.ic_favorite_24);
        else
            favButton.setImageResource(R.drawable.ic_star_border_24);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFavNote(position);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editFavNote(position);
            }
        });

        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unFavNote(position);
            }
        });
    }

    private void deleteFavNote(int position) {
        Log.d("positionToDelete", String.valueOf(position));
        listener.deleteFavNote(position);
    }

    public void editFavNote(int position) {
        listener.editFavNote(position);
    }

    public void unFavNote(int position) {
        listener.unFavNote(position);
    }

    public interface favoritesInterface {
        void deleteFavNote(int fileName);
        void editFavNote(int fileName);
        void unFavNote(int fileName);
    }

    @Override
    public int getItemCount() {
        if (localDataSet != null) {
            return localDataSet.size();
        }
        return 0;
    }
}
