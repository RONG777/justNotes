package com.example.justnotes;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class FavoritesActivity extends BaseActivity implements FavoritesAdapter.favoritesInterface {

    private RecyclerView favRecyclerView;
    private Context parentContext;
    private AppCompatActivity mActivity;
    private FavoritesActivityViewModel favViewModel;
    private DatePickerDialog.OnDateSetListener mDataSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;

    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    double latitude = Double.POSITIVE_INFINITY, longitude = Double.POSITIVE_INFINITY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_favorites);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        parentContext = this.getBaseContext();
        mActivity = this;

        favRecyclerView = findViewById(R.id.favRecyclerView);
        favRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        setLiveDataObservers();

    }

    @Override
    protected void needRefresh() {
        Log.d(TAG, "needRefresh: Favorites");
        setNightMode();
        Intent intent = new Intent(this, FavoritesActivity.class);
        startActivity(intent);
        finish();
    }

    private void setLiveDataObservers() {
        favViewModel = new ViewModelProvider(this).get(FavoritesActivityViewModel.class);
        final Observer<ArrayList<Note>> observerFavorites = new Observer<ArrayList<Note>>() {
            @Override
            public void onChanged(ArrayList<Note> n) {
                FavoritesAdapter newAdapter = new FavoritesAdapter(parentContext, n, (FavoritesAdapter.favoritesInterface) mActivity);
                favRecyclerView.swapAdapter(newAdapter, false);
                newAdapter.notifyDataSetChanged();
            }
        };
        favViewModel.getFavoriteNotes().observe(this, observerFavorites);
    }

    @Override
    public void deleteFavNote(int fileName) {
        favViewModel.deleteNote(fileName);
    }

    @Override
    public void editFavNote(int fileName) {
        editNotePopUp(favRecyclerView, fileName);
    }

    @Override
    public void unFavNote(int fileName) {
        favViewModel.unFavNote(fileName);
    }

    public void editNotePopUp(View anchorView, int fileName) {

        View popupView = getLayoutInflater().inflate(R.layout.note_layout, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        Note toEdit = favViewModel.getNote(fileName);
        EditText title = popupView.findViewById(R.id.note_title);
        EditText description = popupView.findViewById(R.id.editTextDescription);
        EditText text = popupView.findViewById(R.id.note_text);

        Button cancelButton = popupView.findViewById(R.id.cancel_btn);
        cancelButton.setOnClickListener((v) -> {
            popupWindow.dismiss();
        });

        List<String> tags = toEdit.getTags();
        String [] dateTime = toEdit.getCalendarDate().split(" ");

        List<Double> coordinates = toEdit.getCoordinates();
        latitude = Double.POSITIVE_INFINITY;
        longitude = Double.POSITIVE_INFINITY;

        ImageButton mapButton = popupView.findViewById(R.id.add_to_map);
        mapButton.setOnClickListener((v) -> {
            pickPlace();
        });

        ImageButton tagsButton = popupView.findViewById(R.id.add_tag_btn);
        tagsButton.setOnClickListener((v) -> {
            showTags(favRecyclerView, tags);
        });

        ImageButton calendarButton = popupView.findViewById(R.id.calendar_btn_note);
        calendarButton.setOnClickListener((v) -> {
            showDatetime(favRecyclerView, dateTime);
        });

        title.setText(toEdit.getTitle());
        description.setText(toEdit.getDescription());
        text.setText(toEdit.getText());

        Button saveButton = popupView.findViewById(R.id.button2);
        saveButton.setOnClickListener((v) -> {
            String newTitle = title.getText().toString();
            String newDescription = description.getText().toString();
            String newText = text.getText().toString();
            if(TextUtils.isEmpty(newTitle))
                newTitle = "New note";
            if(TextUtils.isEmpty(newDescription))
                newDescription = "Description";
            String calendarDate = dateTime[0] + " " + dateTime[1];
            if(latitude != Double.POSITIVE_INFINITY && longitude != Double.POSITIVE_INFINITY && coordinates.size() != 0) {
                coordinates.set(0, latitude);
                coordinates.set(1, longitude);
            }
            else if (latitude != Double.POSITIVE_INFINITY && longitude != Double.POSITIVE_INFINITY && coordinates.size() == 0) {
                coordinates.add(latitude);
                coordinates.add(longitude);
            }
            favViewModel.editNote(toEdit, newTitle, newDescription, newText, calendarDate, tags, coordinates);
            popupWindow.dismiss();
        });

    }

    private void showTags(View anchorView, List<String> tags) {

        View popupView = getLayoutInflater().inflate(R.layout.tags_layout, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        PopupWindow tagWindow = new PopupWindow(popupView, width, height,true);
        tagWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        tagWindow.showAtLocation(anchorView, Gravity.BOTTOM, 0, 0);

        ChipGroup chipGroup = popupView.findViewById(R.id.chip_group_tags);
        EditText newTag = popupView.findViewById(R.id.new_tag_text);
        Button okButton = popupView.findViewById(R.id.ok_tags_btn);
        ImageButton addTag = popupView.findViewById(R.id.add_tag_btn);

        for(int i = 0; i < tags.size(); i++) {
            Chip newChip = new Chip(this);
            String text = tags.get(i);
            newChip.setText(text);
            newChip.setCloseIconResource(R.drawable.ic_remove_circle_outline_24);
            newChip.setCloseIconEnabled(true);

            //Added click listener on close icon to remove tag from ChipGroup
            newChip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tags.remove(text);
                    chipGroup.removeView(newChip);
                }
            });
            chipGroup.addView(newChip);
        }

        addTag.setOnClickListener((v) -> {
            // Save tags in the note and close pop-up
            if(newTag.getText() != null) {
                Chip newChip = new Chip(this);
                String text = newTag.getText().toString();
                newChip.setText(text);
                newChip.setCloseIconResource(R.drawable.ic_remove_circle_outline_24);
                newChip.setCloseIconEnabled(true);

                //Added click listener on close icon to remove tag from ChipGroup
                newChip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tags.remove(text);
                        chipGroup.removeView(newChip);
                    }
                });

                tags.add(newTag.getText().toString());
                chipGroup.addView(newChip);
                newTag.setText(null);
            }
        });

        okButton.setOnClickListener((v) -> {
            // Save tags in the note and close pop-up
            tagWindow.dismiss();
        });

    }

    private void showDatetime(View anchorView, String [] dateTime) {

        View popupView = getLayoutInflater().inflate(R.layout.date_time_layout, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        PopupWindow datetimeWindow = new PopupWindow(popupView, width, height, true);
        datetimeWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        datetimeWindow.showAtLocation(anchorView, Gravity.BOTTOM, 0, 0);

        TextView mDisplayDate = popupView.findViewById(R.id.select_date);
        TextView mDisplayTime = popupView.findViewById(R.id.select_time);

        if(!dateTime[0].equals("-"))
            mDisplayDate.setText(dateTime[0]);

        if(!dateTime[1].equals("-"))
            mDisplayTime.setText(dateTime[1]);

        mDisplayDate.setOnClickListener((v) -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDataSetListener, year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        mDataSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                String date = d + "/" + m + "/" + y;
                dateTime[0] = date;
                mDisplayDate.setText(date);
            }
        };

        mDisplayTime.setOnClickListener((v) -> {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            TimePickerDialog dialog = new TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mTimeSetListener, hour, minute, true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int h, int m) {
                String time;

                if(m < 10) {
                    String min = "0" + m;
                    time = h + ":" + min;
                }

                else {
                    time = h + ":" + m;
                }

                dateTime[1] = time;
                mDisplayTime.setText(time);
            }
        };

        Button okButton = popupView.findViewById(R.id.ok_datetime_btn);
        okButton.setOnClickListener((v) -> {
            datetimeWindow.dismiss();
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToNotesActivity();
    }

    private void goToNotesActivity() {
        Intent intent = new Intent(this, NotesActivity.class);
        startActivity(intent);
    }

    private void pickPlace() {
        List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME);

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                Log.d("PlaceCoord", place.getLatLng().toString());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
