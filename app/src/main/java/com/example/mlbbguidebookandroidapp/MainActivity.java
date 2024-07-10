package com.example.mlbbguidebookandroidapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.mlbbguidebookandroidapp.R;
import com.example.mlbbguidebookandroidapp.adapter.GuideBookAdapter;
import com.example.mlbbguidebookandroidapp.model.GuideBook;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GuideBookAdapter guideBookAdapter;
    private List<GuideBook> guideBookList;
    private DatabaseReference guideBookRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        guideBookRef = database.getReference("Articles");

        // Inisialisasi artikel list
        guideBookList = new ArrayList<>();
        guideBookAdapter = new GuideBookAdapter(guideBookList);
        recyclerView.setAdapter(guideBookAdapter);

        // Mendengarkan perubahan data di Firebase
        guideBookRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                guideBookList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // Ambil UID
                    String UID = postSnapshot.getKey();
                    GuideBook guideBook = postSnapshot.getValue(GuideBook.class);
                    if (guideBook != null) {
                        guideBook.setUID(UID);
                        guideBookList.add(guideBook);
                    }
                }
                Collections.reverse(guideBookList);
                guideBookAdapter.updateArticles(guideBookList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log atau tangani error
            }
        });

        // Setup bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                return true;
            } else if (itemId == R.id.bottom_search) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
                finish();
                return true;
            } else {
                return false;
            }
        });

        guideBookAdapter.setOnGuideBookClickListener(new GuideBookAdapter.OnGuideBookClickListener() {
            @Override
            public void onGuideBookClick(GuideBook guideBook) {
                Intent intent = new Intent(MainActivity.this, GuideBookDetailActivity.class);
                intent.putExtra("articleId", guideBook.getUID());
                startActivity(intent);
            }
        });

    }
}
