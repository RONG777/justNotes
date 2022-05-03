package com.example.justnotes;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class NotesActivityViewModel extends ViewModel implements DatabaseAdapter.vmInterface {

    private final MutableLiveData<ArrayList<Note>> notes;
    private final MutableLiveData<String> mToast;
    DatabaseAdapter da;

    public NotesActivityViewModel() {
        notes = new MutableLiveData<>();
        mToast = new MutableLiveData<>();
        da = new DatabaseAdapter(this);
        da.getCollection();
    }

    public LiveData<ArrayList<Note>> getNotes() {
        return notes;
    }

    public ArrayList<Note> updateNotes() {
        Log.d("TEST-", "updateNotes() al viewModel");
        da.getCollection();
        return notes.getValue();
    }

    public Note getNote(int idx){
        return notes.getValue().get(idx);
    }
    public void addNote(String noteId, String owner, String title, String description, String text, boolean favorite, String calendarDate, List<String> tags, List<Double> coordinates) {
        Note newNote = new Note(noteId, owner, title, description, text, favorite, calendarDate, tags, coordinates);
        notes.getValue().add(newNote);
        // We inform the observer
        notes.setValue(notes.getValue());
        newNote.saveCard();
    }

    public void deleteNote(int idx) {
        Note note = getNote(idx);
        Log.d("noteToDelete", note.getTitle());
        notes.getValue().remove(idx);
        notes.setValue(notes.getValue());
        da.deleteDocument(note.getNoteId());
    }

    public void favNote(int idx) {
        Note note = getNote(idx);
        boolean favorite = !note.isFavorite();
        note.setFavorite(favorite);
        notes.setValue(notes.getValue());
        da.favoriteDocument(note.getNoteId(), favorite);
    }

    public void editNote(Note toEdit, String newTitle, String newDescription, String newText, String calendarDate, List<String> tags, List<Double> coordinates) {
        toEdit.setTitle(newTitle);
        toEdit.setDescription(newDescription);
        toEdit.setText(newText);
        toEdit.setTags(tags);
        toEdit.setCalendarDate(calendarDate);
        notes.setValue(notes.getValue());
        da.editDocument(toEdit.getNoteId(), newTitle, newDescription, newText, calendarDate, tags, coordinates);
    }

    public LiveData<String> getToast(){
        return mToast;
    }

    @Override
    public void setCollection(ArrayList<Note> n) {
        notes.setValue(n);
    }

    @Override
    public void setToast(String s) {
        mToast.setValue(s);
    }

    public HashMap<String, Note> getCalendarNotes() {
        HashMap<String, Note> calendarNotes = new HashMap<>();
        for(int i = 0; i < notes.getValue().size(); i++) {
            if(!notes.getValue().get(i).getCalendarDate().equals("- -")) {
                calendarNotes.put(notes.getValue().get(i).getCalendarDate(), notes.getValue().get(i));
            }
        }
        return calendarNotes;
    }

}
