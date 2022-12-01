package com.example.b07project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;

public class EditCourses extends AppCompatActivity {
    ListView listView;
    ArrayList<String> arr = new ArrayList<>();
    private DatabaseReference database;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_courses);
        listView = (ListView)findViewById(R.id.list_view);
        btn = findViewById(R.id.button_to_go_back);
        ArrayAdapter<String> arr2 = new ArrayAdapter<>(this, R.layout.textviewlayout, arr);
        database = FirebaseDatabase.getInstance().getReference();

        database.child("admin_courses").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(EditCourses.this, "There Are No Courses Avalialbe", Toast.LENGTH_LONG).show();
                }
                else {
                    for(DataSnapshot ds : task.getResult().getChildren()) {

                        String key = "\n" + ds.child("name").getValue() + " "+ ds.getKey() + "\nOffered: " + ds.child("offerings").getValue() + "\nPrerequisites: " + ds.child("prerequisites").getValue() +"\n";
                        arr.add(key);
                    }
                    listView.setAdapter(arr2);

                }

            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditCourses.this, AdminHomePage.class));
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditCourses.this);
                builder.setTitle("Select");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteCourse(arr.get(i));
                        finish();
                        startActivity(getIntent());
                    }
                });
                builder.setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Call edit func
                        finish();
                        startActivity(getIntent());
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();



            }
            private void deleteCourse(String courseCode) {

                database.child("admin_courses").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                        else {
                            for (DataSnapshot ds: task.getResult().getChildren()) {
                                String[] prereqs = task.getResult().child(ds.getKey()).child("prerequisites").getValue().toString().split(",");
                                int i = 0;
                                boolean courseInList = false;
                                while (i < prereqs.length && !courseInList) {
                                    if (prereqs[i].equals(courseCode)) {
                                        courseInList = true;
                                        String newPrereqs = "";
                                        for (int j = 0; j < prereqs.length; j++) {
                                            if (j != i) {
                                                newPrereqs += prereqs[j] + ",";
                                            }
                                        }
                                        database.child("admin_courses").child(ds.getKey()).child("prerequisites").setValue(newPrereqs.substring(0, newPrereqs.length()-1));
                                    }
                                    i++;
                                }
                            }
                            database.child("admin_courses").child(courseCode).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(EditCourses.this, "Course Deleted Successfully", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(EditCourses.this, "Something's Wrong", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }
                });
            }


        });



    }
}
