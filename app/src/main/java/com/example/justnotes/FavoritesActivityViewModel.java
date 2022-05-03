package com.example.justnotes;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivityViewModel extends ViewModel implements DatabaseAdapter.vmInterface {
    private final MutableLiveData<ArrayList<Note>> favoriteNotes;
    DatabaseAdapter da;

    public FavoritesActivityViewModel() {
        favoriteNotes = new MutableLiveData<>();
        da = new DatabaseAdapter(this);
        da.getFavoritesCollection();
    }

    public LiveData<ArrayList<Note>> getFavoriteNotes() {
        return favoriteNotes;
    }
    public Note getNote(int idx){
        return favoriteNotes.getValue().get(idx);
    }

    public void deleteNote(int idx) {
        Note note = getNote(idx);
        Log.d("noteToDelete", note.getTitle());
        favoriteNotes.getValue().remove(idx);
        favoriteNotes.setValue(favoriteNotes.getValue());
        da.deleteDocument(note.getNoteId());
    }

    public void unFavNote(int idx) {
        Note note = getNote(idx);
        favoriteNotes.getValue().remove(idx);
        note.setFavorite(false);
        favoriteNotes.setValue(favoriteNotes.getValue());
        da.favoriteDocument(note.getNoteId(), false);
    }

    public void editNote(Note toEdit, String newTitle, String newDescription, String newText, String calendarDate, List<String> tags, List<Double> coordinates) {
        toEdit.setTitle(newTitle);
        toEdit.setDescription(newDescription);
        toEdit.setText(newText);
        toEdit.setTags(tags);
        favoriteNotes.setValue(favoriteNotes.getValue());
        da.editDocument(toEdit.getNoteId(), newTitle, newDescription, newText, calendarDate, tags, coordinates);
    }

    @Override
    public void setCollection(ArrayList<Note> n) {
        favoriteNotes.setValue(n);
    }

    @Override
    public void setToast(String s) {

    }
}
