package com.groupb.lewisadvising;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUpActivity  extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ArrayList<String> concentrations = new ArrayList<>();
    ArrayList<String> startingTerm = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        final TextView name = findViewById(R.id.name);
        final TextView phone = findViewById(R.id.phone);
        final TextView email = findViewById(R.id.email);
        final TextView password = findViewById(R.id.password);
        final TextView address = findViewById(R.id.address);
        final Spinner concentrationSpinner = findViewById(R.id.concentration);
        final Spinner startingTermSpinner = findViewById(R.id.startingTerm);
        Button registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();
        final ArrayAdapter<String> concentrationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, concentrations);
        concentrationAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        concentrationSpinner.setAdapter(concentrationAdapter);

        final ArrayAdapter<String> startingTermAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, startingTerm);
        startingTermAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        startingTermSpinner.setAdapter(startingTermAdapter);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("concentration")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                concentrations.add(document.getId());
                            }
                            concentrationAdapter.notifyDataSetChanged();
                        }
                    }
                });

        concentrationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                db.collection("concentration").document(concentrations.get(i)).collection("startingTerm")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    startingTerm.clear();
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        startingTerm.add(document.getId());
                                    }
                                    startingTermAdapter.notifyDataSetChanged();
                                }
                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("name", name.getText().toString());
                                    user.put("phone", phone.getText().toString());
                                    user.put("address", address.getText().toString());
                                    user.put("email", email.getText().toString());
                                    user.put("concentration", concentrationSpinner.getSelectedItem().toString());
                                    user.put("startingTerm", startingTermSpinner.getSelectedItem().toString());

                                    db.collection("users").document(mAuth.getCurrentUser().getUid())
                                            .set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Intent i = new Intent(SignUpActivity.this, ProfileActivity.class);
                                                    startActivity(i);
                                                }

                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }
}
