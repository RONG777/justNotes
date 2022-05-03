package com.example.justnotes;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class MapsActivityViewModel extends ViewModel implements DatabaseAdapter.vmInterface {
    private final MutableLiveData<ArrayList<Note>> mapNotes;
    DatabaseAdapter da;

    public MapsActivityViewModel() {
        mapNotes = new MutableLiveData<>();
        da = new DatabaseAdapter(this);
        da.getMapCollection();
    }

    public MutableLiveData<ArrayList<Note>> getMapNotes() {
        return mapNotes;
    }

    public Note getNote(int idx){
        return mapNotes.getValue().get(idx);
    }

    @Override
    public void setCollection(ArrayList<Note> n) {
        mapNotes.setValue(n);
        Log.d("CNTRL COORDSIZE SC MAVM", String.valueOf(n.size()));
    }

    @Override
    public void setToast(String s) {
    }
}
