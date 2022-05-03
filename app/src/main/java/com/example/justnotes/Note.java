package com.example.justnotes;

import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Note {
    private String noteId;
    private String owner;
    private String title; // Title of the note
    private String description; // Text of the note
    private String text; // Text of the note
    private boolean favorite;
    private String calendarDate;
    private List<String> tags = new ArrayList<>();
    private List<Double> coordinates = new ArrayList<>();
    private final DatabaseAdapter adapter = DatabaseAdapter.databaseAdapter;

    public Note(String noteId, String owner, String title, String description, String text, boolean favorite, String calendarDate, List<String> tags, List<Double> coordinates) {

        this.noteId = noteId;
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.text = text;
        this.favorite = favorite;
        this.calendarDate = calendarDate;
        this.tags = tags;
        this.coordinates = coordinates;

    }

    public String getTitle () {
        return this.title;
    }
    public String getDescription () {
        return this.description;
    }
    public String getText () {
        return this.text;
    }
    public String getNoteId() {
        return this.noteId;
    }
    public String getCalendarDate() {
        return calendarDate;
    }
    public boolean isFavorite() {
        return favorite;
    }
    public List<String> getTags() {
        return tags;
    }
    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setNoteId (String id) {
        this.noteId = id;
    }
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
    public void setCalendarDate(String calendarDate) {
        this.calendarDate = calendarDate;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public void saveCard() {
        Log.d("saveCard", "saveCard-> saveDocument");
        adapter.saveDocument(this.noteId, this.owner, this.title, this.description, this.text, this.favorite, this.calendarDate, this.tags, this.coordinates);
    }

}
