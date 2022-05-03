package com.example.justnotes;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Continuation;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class DatabaseAdapter {

    public static final String TAG = "DatabaseAdapter";

    public static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user;


    public static vmInterface listener;
    public static DatabaseAdapter databaseAdapter;

    public DatabaseAdapter(vmInterface listener){
        this.listener = listener;
        databaseAdapter = this;
        FirebaseFirestore.setLoggingEnabled(true);
        user = mAuth.getCurrentUser();
    }


    public interface vmInterface{
        void setCollection(ArrayList<Note> n);
        void setToast(String s);
    }

    public void getCollection(){
        Log.d(TAG,"updatenotes");
        DatabaseAdapter.db.collection("notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Note> retrieved_n = new ArrayList<Note>() ;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if(document.getString("owner").equals(user.getUid()))
                                    retrieved_n.add(new Note(document.getString("id"), document.getString("owner"), document.getString("title"), document.getString("description"), document.getString("text"), document.getBoolean("favorite"), document.getString("calendarDate"), (List<String>) document.get("tags"), (List<Double>) document.get("coordinates")));
                            }
                            listener.setCollection(retrieved_n);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void getFavoritesCollection(){
        Log.d(TAG,"updatenotes");
        DatabaseAdapter.db.collection("notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Note> retrieved_n = new ArrayList<Note>() ;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if(document.getString("owner").equals(user.getUid()) && document.getBoolean("favorite"))
                                    retrieved_n.add(new Note(document.getString("id"), document.getString("owner"), document.getString("title"), document.getString("description"), document.getString("text"), document.getBoolean("favorite"), document.getString("calendarDate"), (List<String>) document.get("tags"), (List<Double>) document.get("coordinates")));
                            }
                            listener.setCollection(retrieved_n);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void getMapCollection(){
        Log.d(TAG,"updatenotes");
        DatabaseAdapter.db.collection("notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Note> retrieved_n = new ArrayList<Note>() ;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                List <Double> coords = (List<Double>) document.get("coordinates");
                                if(document.getString("owner").equals(user.getUid()) && coords.size() != 0)
                                    retrieved_n.add(new Note(document.getString("id"), document.getString("owner"), document.getString("title"), document.getString("description"), document.getString("text"), document.getBoolean("favorite"), document.getString("calendarDate"), (List<String>) document.get("tags"), (List<Double>) document.get("coordinates")));
                            }
                            Log.d("CNTRL COORDSIZE DB", String.valueOf(retrieved_n.size()));
                            listener.setCollection(retrieved_n);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void saveDocument (String id, String owner, String title, String description, String text, boolean favorite, String calendarDate, List<String> tags, List<Double> coordinates) {

        Map<String, Object> note = new HashMap<>();
        note.put("id", id);
        note.put("owner", owner);
        note.put("title", title);
        note.put("description", description);
        note.put("text", text);
        note.put("favorite", favorite);
        note.put("calendarDate", calendarDate);
        note.put("tags", tags);
        note.put("coordinates", coordinates);

        Log.d(TAG, "saveDocument");

        db.collection("notes").document(id)
                .set(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully written!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }

    public void deleteDocument(String noteId) {
        DocumentReference note = db.collection("notes").document(noteId);
        note.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    public void editDocument(String noteId, String newTitle, String newDescription, String newText, String calendarDate, List<String> tags, List<Double> coordinates) {
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("title", newTitle);
        updates.put("description", newDescription);
        updates.put("text", newText);
        updates.put("calendarDate", calendarDate);
        updates.put("tags", tags);
        updates.put("coordinates", coordinates);
        db.collection("notes").document(noteId).update(updates);
    }

    public void favoriteDocument(String noteId, boolean favorite) {
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("favorite", favorite);
        db.collection("notes").document(noteId).update(updates);
    }

}