package com.example.finalapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

public class TypeManagementActivity extends AppCompatActivity {

    private RecyclerView rvTypes;
    private TypeAdapter adapter;
    private List<Type> typeList = new ArrayList<>();
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_management);

        rvTypes = findViewById(R.id.rvTypes);
        rvTypes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TypeAdapter(typeList, new TypeAdapter.Callback() {
            @Override
            public void onEdit(Type t) { showEditDialog(t); }

            @Override
            public void onDelete(Type t) { deleteType(t); }
        });
        rvTypes.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddType);
        fab.setOnClickListener(v -> showAddDialog());

        loadTypes();
    }

    private void loadTypes() {
        dbRef.child("types").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                typeList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Type t = child.getValue(Type.class);
                    if (t != null) {
                        t.setId(child.getKey());
                        typeList.add(t);
                    }
                }
                adapter.update(typeList);
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void showAddDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_type, null);
        EditText etName = v.findViewById(R.id.etTypeName);
        new AlertDialog.Builder(this)
                .setTitle("Add Type")
                .setView(v)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) { Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show(); return; }
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    dbRef.child("types").push().setValue(data).addOnSuccessListener(aVoid -> Toast.makeText(this, "Type added", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Type t) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_type, null);
        EditText etName = v.findViewById(R.id.etTypeName);
        etName.setText(t.getName());
        new AlertDialog.Builder(this)
                .setTitle("Edit Type")
                .setView(v)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) { Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show(); return; }
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    dbRef.child("types").child(t.getId()).setValue(data).addOnSuccessListener(aVoid -> Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteType(Type t) {
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Delete type " + t.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> dbRef.child("types").child(t.getId()).removeValue().addOnSuccessListener(aVoid -> Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }
}

