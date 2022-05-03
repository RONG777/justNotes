package com.example.justnotes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.model.ObjectValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static android.widget.Toast.LENGTH_SHORT;

public class NotesActivity extends BaseActivity implements CustomAdapter.notesInterface {

    private final String TAG = "MainActivity";

    private Context parentContext;
    private AppCompatActivity mActivity;

    private NotesActivityViewModel viewModel;
    private RecyclerView mRecyclerView;
    private SearchView searchView;
    private CustomAdapter customAdapter;

    // Optional attributes to sign out from Google
    FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private DatePickerDialog.OnDateSetListener mDataSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;

    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    double latitude = Double.POSITIVE_INFINITY, longitude = Double.POSITIVE_INFINITY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        parentContext = this.getBaseContext();
        mActivity = this;

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchView = findViewById(R.id.searchView);

        mAuth =  FirebaseAuth.getInstance();

        // Configure gso's for Google Sign In in order to Sign Out later
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCRW-dOgjCTybcV8hHBxTSQ7kmJd1yz_AY");
        }

        setLiveDataObservers();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                customAdapter = (CustomAdapter) mRecyclerView.getAdapter();
                customAdapter.filter(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                customAdapter = (CustomAdapter) mRecyclerView.getAdapter();
                customAdapter.filter(s);
                return true;
            }
        });

    }

    @Override
    protected void needRefresh() {
        Log.d(TAG, "needRefresh: Main");
        setNightMode();
        Intent intent = new Intent(this, NotesActivity.class);
        startActivity(intent);
        finish();
    }

    private void setLiveDataObservers() {

        viewModel = new ViewModelProvider(this).get(NotesActivityViewModel.class);

        final Observer<ArrayList<Note>> observer = new Observer<ArrayList<Note>>() {
            @Override
            public void onChanged(ArrayList<Note> n) {
                if(searchView.getQuery().length() != 0 && searchView.getQuery() != null) {
                    CustomAdapter newAdapter = new CustomAdapter(parentContext, viewModel.updateNotes(), (CustomAdapter.notesInterface) mActivity);
                    Log.d("TEST-", "updateNotes() a NotesActivity");
                    mRecyclerView.swapAdapter(newAdapter, false);
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                }
                else {
                    CustomAdapter newAdapter = new CustomAdapter(parentContext, n, (CustomAdapter.notesInterface) mActivity);
                    mRecyclerView.swapAdapter(newAdapter, false);
                    newAdapter.notifyDataSetChanged();
                }
            }
        };

        final Observer<String> observerToast = new Observer<String>() {
            @Override
            public void onChanged(String t) {
                Toast.makeText(parentContext, t, LENGTH_SHORT).show();
            }
        };

        viewModel.getNotes().observe(this, observer);
        viewModel.getToast().observe(this, observerToast);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notes_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.configuration:
                searchView.setQuery("", false);
                searchView.setIconified(true);
                showConfiguration(mRecyclerView);
                return true;

            case R.id.add_note:
                showNewNote(mRecyclerView);
                return true;

            case R.id.view_calendar:
                searchView.setQuery("", false);
                searchView.setIconified(true);
                showCalendar(mRecyclerView);
                return true;

            case R.id.favorites:
                searchView.setQuery("", false);
                searchView.setIconified(true);
                goToFavoritesActivity();
                return true;

            case R.id.map_btn:
                gotToMapActivity();
                return true;

            case R.id.DN:
                goToNightActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void gotToMapActivity() {
        Intent intent = new Intent(NotesActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    private void goToNightActivity() {
        Intent intentNight = new Intent(NotesActivity.this, NightActivity.class);
        startActivity(intentNight);
    }

    private void showCalendar(View anchorView) {

        View popupView = getLayoutInflater().inflate(R.layout.calendar_layout, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        CalendarView calendarView = popupView.findViewById(R.id.calendarView);
        HashMap<String, Note> calendarNotes = viewModel.getCalendarNotes();

        List<String> calendarDates = new ArrayList<String>(calendarNotes.keySet());

        List<Calendar> highlights = new ArrayList<>();
        int y, m, d;
        for(int i = 0; i < calendarDates.size(); i++) {
            Calendar cal = Calendar.getInstance();
            List<String> ymd = Arrays.asList(calendarDates.get(i).split(" ")[0].split("/"));
            y = Integer.parseInt(ymd.get(2));
            m = Integer.parseInt(ymd.get(1)) - 1;
            d = Integer.parseInt(ymd.get(0));
            cal.set(y, m, d);
            highlights.add(cal);
        }

        calendarView.setHighlightedDays(highlights);

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDayCalendar = eventDay.getCalendar();
                int cY = clickedDayCalendar.get(Calendar.YEAR);
                int cM = clickedDayCalendar.get(Calendar.MONTH) + 1;
                int cD = clickedDayCalendar.get(Calendar.DAY_OF_MONTH);
                String date = Integer.toString(cD) + "/" + Integer.toString(cM) + "/" + Integer.toString(cY);
                for(int i = 0; i < calendarDates.size(); i++) {
                    if(calendarDates.get(i).split(" ")[0].equals(date)) {
                        Toast toast = Toast.makeText(getApplicationContext(),calendarNotes.get(calendarDates.get(i)).getTitle(), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        });

    }

    private void showNewNote(View anchorView) {
        View popupView = getLayoutInflater().inflate(R.layout.note_layout, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        EditText saveDescr = popupView.findViewById(R.id.editTextDescription);
        EditText saveTitle = popupView.findViewById(R.id.note_title);
        EditText saveText = popupView.findViewById(R.id.note_text);
        Button saveButton = popupView.findViewById(R.id.button2);
        List<String> tags = new ArrayList<>();
        String [] dateTime = {"-",  "-"};
        List<Double> coords = new ArrayList<>();
        latitude = Double.POSITIVE_INFINITY;
        longitude = Double.POSITIVE_INFINITY;

        ImageButton mapButton = popupView.findViewById(R.id.add_to_map);
        mapButton.setOnClickListener((v) -> {
            pickPlace();
        });

        ImageButton calendarButton = popupView.findViewById(R.id.calendar_btn_note);
        calendarButton.setOnClickListener((v) -> {
            showDatetime(mRecyclerView, dateTime,saveTitle.getText().toString(),saveDescr.getText().toString());
        });

        ImageButton tagsButton = popupView.findViewById(R.id.add_tag_btn);
        tagsButton.setOnClickListener((v) -> {
            showTags(mRecyclerView, tags);
        });

        Button cancelButton = popupView.findViewById(R.id.cancel_btn);
        cancelButton.setOnClickListener((v) -> {
            popupWindow.dismiss();
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        saveButton.setOnClickListener((v) -> {
            String title = saveTitle.getText().toString();
            String description = saveDescr.getText().toString();
            String text = saveText.getText().toString();
            if(TextUtils.isEmpty(title))
                title = "New note";
            if(TextUtils.isEmpty(description))
                description = "Description";
            String calendarDate = dateTime[0] + " " + dateTime[1];
            if(latitude != Double.POSITIVE_INFINITY && longitude != Double.POSITIVE_INFINITY) {
                coords.add(latitude);
                coords.add(longitude);
            }
            viewModel.addNote(UUID.randomUUID().toString(), uid, title, description, text, false, calendarDate, tags, coords);
            popupWindow.dismiss();
        });

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

    private void showDatetime(View anchorView, String [] dateTime, String title, String description) {
        Calendar actual = Calendar.getInstance();
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
                actual.set(Calendar.DAY_OF_MONTH,d);
                actual.set(Calendar.MONTH,m);
                actual.set(Calendar.YEAR,y);

                m += 1;
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
                actual.set(Calendar.HOUR_OF_DAY,h);
                actual.set(Calendar.MINUTE,m);

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
            //Notification
            if(actual.getTimeInMillis() > System.currentTimeMillis()){
                String tag = generateKey();
                Long alertTime = actual.getTimeInMillis()-System.currentTimeMillis();
                int random = (int)(Math.random()*50+1);

                Data data = saveNotification(title,description,random);
                Notification.guardarNotification(alertTime,data,tag);

                Toast.makeText(NotesActivity.this,"Notification saved", LENGTH_SHORT).show();
            }
            datetimeWindow.dismiss();
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
;            });
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
            tagWindow.dismiss();
        });

    }

    private void showConfiguration(View anchorView) {

        View popupView = getLayoutInflater().inflate(R.layout.config_layout, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        // Initialize objects from layout
        Button signOutButton = popupView.findViewById(R.id.signOut_button);
        signOutButton.setOnClickListener((v) -> {
            FirebaseAuth.getInstance().signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent IntentMainActivity = new Intent(getApplicationContext(), SignInActivity.class);
                    IntentMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(IntentMainActivity);
                    NotesActivity.this.finish();
                }
            });
            popupWindow.dismiss();
        });
    }


    private void goToFavoritesActivity() {
        Intent intent = new Intent(this, FavoritesActivity.class);
        startActivity(intent);
    }

    @Override
    public void deleteNote(int recyclerItem) {
        viewModel.deleteNote(recyclerItem);
    }

    @Override
    public void editNote(int fileName) {
        editNotePopUp(mRecyclerView, fileName);
    }

    @Override
    public void favNote(int recyclerItem) {
        viewModel.favNote(recyclerItem);
    }

    public void editNotePopUp(View anchorView, int fileName) {

        View popupView = getLayoutInflater().inflate(R.layout.note_layout, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        Note toEdit = viewModel.getNote(fileName);
        EditText title = popupView.findViewById(R.id.note_title);
        EditText description = popupView.findViewById(R.id.editTextDescription);
        EditText text = popupView.findViewById(R.id.note_text);

        ImageButton mapButton = popupView.findViewById(R.id.add_to_map);
        mapButton.setOnClickListener((v) -> {
            pickPlace();
        });

        Button cancelButton = popupView.findViewById(R.id.cancel_btn);
        cancelButton.setOnClickListener((v) -> {
            popupWindow.dismiss();
        });

        List<String> tags = toEdit.getTags();
        String [] dateTime = toEdit.getCalendarDate().split(" ");

        List<Double> coords = toEdit.getCoordinates();
        latitude = Double.POSITIVE_INFINITY;
        longitude =  Double.POSITIVE_INFINITY;

        ImageButton tagsButton = popupView.findViewById(R.id.add_tag_btn);
        tagsButton.setOnClickListener((v) -> {
            showTags(mRecyclerView, tags);
        });

        ImageButton calendarButton = popupView.findViewById(R.id.calendar_btn_note);
        calendarButton.setOnClickListener((v) -> {
            showDatetime(mRecyclerView, dateTime, title.getText().toString(),description.getText().toString());
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
            if(latitude != Double.POSITIVE_INFINITY && longitude != Double.POSITIVE_INFINITY && coords.size() != 0) {
                coords.set(0, latitude);
                coords.set(1, longitude);
            }
            else if (latitude != Double.POSITIVE_INFINITY && longitude != Double.POSITIVE_INFINITY && coords.size() == 0) {
                coords.add(latitude);
                coords.add(longitude);
            }
            viewModel.editNote(toEdit, newTitle, newDescription, newText, calendarDate, tags, coords);
            popupWindow.dismiss();
        });

        ImageButton shareButton=popupView.findViewById(R.id.share_btn);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent shareIntent=new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String share_text=text.getText().toString();
                shareIntent.putExtra(Intent.EXTRA_TEXT,share_text);
                Intent sendIntent=Intent.createChooser(shareIntent,null);
                startActivity(sendIntent);
            }
        });
    }

    //Métodos para las notificaciones
    private String generateKey(){
        return UUID.randomUUID().toString();
    }

    private Data saveNotification(String titulo,String detalle,int idNotification){
        return new Data.Builder()
                .putString("titulo",titulo)
                .putString("detalle",detalle)
                .putInt("idNotification",idNotification).build();
    }

}