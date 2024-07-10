package com.example.mlbbguidebookandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private Button btnSearch;
    private RecyclerView recyclerView;
    private GuideBookAdapter adapter;
    private DatabaseReference guideBookRef;
    private Spinner spinnerCategories;

    private List<GuideBook> guideBookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        recyclerView = findViewById(R.id.recyclerView);
        spinnerCategories = findViewById(R.id.spinner_categories);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        guideBookList = new ArrayList<>();
        adapter = new GuideBookAdapter(guideBookList);
        recyclerView.setAdapter(adapter);

        // Firebase reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        guideBookRef = database.getReference("Articles");

        // Load all articles initially
        loadArticles();

        // Search button click listener
        btnSearch.setOnClickListener(v -> performSearch());

        adapter.setOnGuideBookClickListener(new GuideBookAdapter.OnGuideBookClickListener() {
            @Override
            public void onGuideBookClick(GuideBook guideBook) {
                String articleId = guideBook.getUID();

                if (articleId != null && !articleId.isEmpty()) {
                    Intent intent = new Intent(SearchActivity.this, GuideBookDetailActivity.class);
                    intent.putExtra("articleId", articleId);
                    startActivity(intent);
                } else {
                    Toast.makeText(SearchActivity.this, "Article ID is null or empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setup bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
                finish();
                return true;
            } else if (itemId == R.id.bottom_search) {
                return true;
            } else {
                return false;
            }
        });

        // Text change listener for search EditText
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterBySearchQuery(s.toString());
            }
        });

        // Setup category spinner
        setupCategorySpinner();
    }

    private void loadArticles() {
        guideBookRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                guideBookList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String UID = postSnapshot.getKey();
                    GuideBook guideBook = postSnapshot.getValue(GuideBook.class);
                    if (guideBook != null) {
                        guideBook.setUID(UID);
                        guideBookList.add(guideBook);
                    }
                }
                Collections.reverse(guideBookList);
                adapter.updateArticles(guideBookList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim().toLowerCase();
        filterBySearchQuery(query);
    }

    private void filterBySearchQuery(String query) {
        List<GuideBook> filteredList = new ArrayList<>();
        for (GuideBook guideBook : guideBookList) {
            if (guideBook.getArticleTitle().toLowerCase().contains(query)
                    || guideBook.getArticleContent().toLowerCase().contains(query)) {
                filteredList.add(guideBook);
            }
        }
        adapter.updateArticles(filteredList);
    }

    private void filterByCategory(String category) {
        List<GuideBook> filteredList = new ArrayList<>();
        for (GuideBook guideBook : guideBookList) {
            if (guideBook.getArticleCategory().equalsIgnoreCase(category)) {
                filteredList.add(guideBook);
            }
        }
        adapter.updateArticles(filteredList);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.categories_array, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategories.setAdapter(categoryAdapter);
        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = parent.getItemAtPosition(position).toString();
                if (!category.equals("All")) {
                    filterByCategory(category);
                } else {
                    // Update articles with the full guideBookList
                    adapter.updateArticles(guideBookList);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not used
            }
        });
    }
}
