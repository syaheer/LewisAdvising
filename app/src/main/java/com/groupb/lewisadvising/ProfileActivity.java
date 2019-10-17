package com.groupb.lewisadvising;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String concentrationString;
    private String startingTermString;
    private List<String> terms = new ArrayList<>();
    private FirebaseFirestore db;
    private LinearLayout dynamicLayout;

    private LinkedHashMap<String, List<String>> courses = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dynamicLayout = findViewById(R.id.dynamicLayout);

        terms.add("Fall 1-2019");
        terms.add("Fall 2-2019");
        terms.add("Spring 1-2020");
        terms.add("Spring 2-2020");
        terms.add("Summer 1-2020");
        terms.add("Summer 2-2020");
        terms.add("Fall 1-2020");
        terms.add("Fall 2-2020");
        terms.add("Spring 1-2021");
        terms.add("Spring 2-2021");
        terms.add("Summer 1-2021");
        terms.add("Summer 2-2021");
        terms.add("Fall 1-2021");
        terms.add("Fall 2-2021");
        terms.add("Spring 1-2022");
        terms.add("Spring 2-2022");
        terms.add("Summer 1-2022");
        terms.add("Summer 2-2022");
        terms.add("Fall 1-2022");
        terms.add("Fall 2-2022");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            getData();
        } else {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class)); //Go back to home page
            finish();
        }
    }

    private void getData() {
        //profile
        final TextView name = findViewById(R.id.name);
        final TextView phone = findViewById(R.id.phone);
        final TextView email = findViewById(R.id.email);
        final TextView address = findViewById(R.id.address);
        final TextView concentration = findViewById(R.id.concentration);
        final TextView startingTerm = findViewById(R.id.startingTerm);

        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    name.setText(document.getString("name"));
                    phone.setText(document.getString("phone"));
                    email.setText(document.getString("email"));
                    address.setText(document.getString("address"));
                    concentration.setText(document.getString("concentration"));
                    startingTerm.setText(document.getString("startingTerm"));

                    concentrationString = document.getString("concentration");
                    startingTermString = document.getString("startingTerm");

                    Log.d("terms", concentrationString + startingTermString);

                    getNumberOfTerms();
                }
            }
        });
    }

    private void getNumberOfTerms() {
        // number of terms
        db.collection("concentration").document(concentrationString).collection("startingTerm").document(startingTermString)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        long numberOfTerms = (long) task.getResult().get("terms");
                        Log.d("numberOfTerms", String.valueOf(numberOfTerms));
                        getCoursesList(numberOfTerms);
                    }
                });
    }

    private void getCoursesList(long numberOfTerms) {
        int startingIndex = terms.indexOf(startingTermString);
        for (int i = 0; i < numberOfTerms; i++) {
            final int currIndex = i + startingIndex;
            Log.d("currIndex", currIndex + " " + terms.get(currIndex));
            final LayoutInflater inflater = getLayoutInflater();
            final View termView = inflater.inflate(R.layout.item_terms, null);
            TextView termTv = termView.findViewById(R.id.term);
            termTv.setText(terms.get(currIndex));
            ViewGroup main = findViewById(R.id.dynamicLayout);
            main.addView(termView, i);
            // Courses
            db.collection("concentration").document(concentrationString).collection("startingTerm").document(startingTermString).collection(terms.get(currIndex))
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                //terms.clear();
                                for (final QueryDocumentSnapshot document : task.getResult()) {
                                    final String termId = document.getId();
                                    db.collection("courses").document(termId)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("adding", termId);
                                                        View view = inflater.inflate(R.layout.item_courses, null);
                                                        TextView termTv = view.findViewById(R.id.course);
                                                        if (task.getResult().get("name") != null)
                                                            termTv.setText(termId + " " + task.getResult().get("name"));
                                                        else
                                                            termTv.setText(termId);
                                                        ViewGroup main = termView.findViewById(R.id.coursesList);
                                                        main.addView(view, 0);
                                                    }
                                                }
                                            });


                                }

                            }
                        }
                    });

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this, MainActivity.class)); //Go back to home page
                finish();
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}