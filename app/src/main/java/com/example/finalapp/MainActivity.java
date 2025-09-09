package com.example.finalapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PetAdapter adapter;
    private List<Pet> petList = new ArrayList<>();
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // set toolbar as support action bar so menu callbacks work
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PetAdapter(petList, new PetAdapter.Callback() {
            @Override
            public void onEdit(Pet pet) {
                Intent i = new Intent(MainActivity.this, EditPetActivity.class);
                i.putExtra("petId", pet.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(Pet pet) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete")
                        .setMessage("Delete pet '" + pet.getName() + "' ?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (pet.getId() != null) {
                                dbRef.child("pets").child(pet.getId()).removeValue();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddPet);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddPetActivity.class)));

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        loadPets();
    }

    private void loadPets() {
        DatabaseReference petsRef = dbRef.child("pets");
        petsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                petList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Pet p = child.getValue(Pet.class);
                    if (p != null) {
                        p.setId(child.getKey());
                        petList.add(p);
                    }
                }
                adapter.updateList(petList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // ignore for now
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_manage_breeds) {
            startActivity(new Intent(this, BreedManagementActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_manage_types) {
            startActivity(new Intent(this, TypeManagementActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}