package com.example.finalapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreedManagementActivity extends AppCompatActivity {

    private RecyclerView rvBreeds;
    private BreedAdapter adapter;
    private List<Breed> breedList = new ArrayList<>();
    private List<String> types = new ArrayList<>();

    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breed_management);

        rvBreeds = findViewById(R.id.rvBreeds);
        rvBreeds.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BreedAdapter(breedList, new BreedAdapter.Callback() {
            @Override
            public void onEdit(Breed b) { showEditDialog(b); }
            @Override
            public void onDelete(Breed b) { deleteBreed(b); }
        });
        rvBreeds.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddBreed);
        fab.setOnClickListener(v -> showAddDialog());

        loadTypes();
        loadBreeds();
    }

    private void loadTypes() {
        dbRef.child("types").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                types.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String name = child.child("name").getValue(String.class);
                    if (name != null) types.add(name);
                }
                if (types.isEmpty()) { types.add("Dog"); types.add("Cat"); types.add("Bird"); }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void loadBreeds() {
        dbRef.child("breeds").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                breedList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Breed b = child.getValue(Breed.class);
                    if (b != null) {
                        b.setId(child.getKey());
                        breedList.add(b);
                    }
                }
                adapter.update(breedList);
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void showAddDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_breed, null);
        EditText etName = v.findViewById(R.id.etBreedName);
        Spinner spinnerType = v.findViewById(R.id.spinnerTypeForBreed);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add Breed")
                .setView(v)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String type = (String) spinnerType.getSelectedItem();
                    if (name.isEmpty()) { Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show(); return; }
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    data.put("type", type);
                    dbRef.child("breeds").push().setValue(data).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Breed added", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Breed b) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_breed, null);
        EditText etName = v.findViewById(R.id.etBreedName);
        Spinner spinnerType = v.findViewById(R.id.spinnerTypeForBreed);
        etName.setText(b.getName());

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        int sel = types.indexOf(b.getType()); if (sel >= 0) spinnerType.setSelection(sel);

        new AlertDialog.Builder(this)
                .setTitle("Edit Breed")
                .setView(v)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String type = (String) spinnerType.getSelectedItem();
                    if (name.isEmpty()) { Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show(); return; }
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    data.put("type", type);
                    dbRef.child("breeds").child(b.getId()).setValue(data).addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteBreed(Breed b) {
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Delete breed " + b.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> dbRef.child("breeds").child(b.getId()).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
