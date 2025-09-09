package com.example.finalapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditPetActivity extends AppCompatActivity {

    private EditText etName, etPrice;
    private Spinner spinnerGender, spinnerType, spinnerBreed;
    private TextView tvDob;
    private Button btnPickDob, btnUpdate, btnDelete;

    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    private List<String> genders = new ArrayList<>();
    private List<String> types = new ArrayList<>();
    private List<Breed> breeds = new ArrayList<>();

    private ArrayAdapter<String> genderAdapter;
    private ArrayAdapter<String> typeAdapter;
    private ArrayAdapter<String> breedAdapter;

    private String selectedDob = "";
    private String petId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pet);

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerBreed = findViewById(R.id.spinnerBreed);
        tvDob = findViewById(R.id.tvDob);
        btnPickDob = findViewById(R.id.btnPickDob);
        btnUpdate = findViewById(R.id.btnUpdatePet);
        btnDelete = findViewById(R.id.btnDeletePet);

        genders.add("Male"); genders.add("Female"); genders.add("Unknown");
        genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        breedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(breedAdapter);

        loadTypes();
        loadBreeds();

        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= types.size()) return;
                String selectedType = types.get(position);
                List<String> filtered = new ArrayList<>();
                for (Breed b : breeds) if (selectedType.equals(b.getType())) filtered.add(b.getName());
                breedAdapter.clear();
                breedAdapter.addAll(filtered);
                breedAdapter.notifyDataSetChanged();
            }

            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnPickDob.setOnClickListener(v -> showDatePicker());

        petId = getIntent().getStringExtra("petId");
        if (petId == null) { Toast.makeText(this, "No pet id", Toast.LENGTH_SHORT).show(); finish(); return; }
        loadPet(petId);

        btnUpdate.setOnClickListener(v -> updatePet());
        btnDelete.setOnClickListener(v -> deletePet());
    }

    private void loadTypes() {
        dbRef.child("types").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                types.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String name = child.child("name").getValue(String.class);
                    if (name != null) types.add(name);
                }
                if (types.isEmpty()) { types.add("Dog"); types.add("Cat"); types.add("Bird"); }
                typeAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void loadBreeds() {
        dbRef.child("breeds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                breeds.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Breed b = child.getValue(Breed.class);
                    if (b != null) { b.setId(child.getKey()); breeds.add(b); }
                }
                // if types already selected, filter
                if (!types.isEmpty() && spinnerType.getSelectedItemPosition() >= 0) {
                    int pos = spinnerType.getSelectedItemPosition();
                    if (pos < types.size()) {
                        String sel = types.get(pos);
                        List<String> filtered = new ArrayList<>();
                        for (Breed br : breeds) if (sel.equals(br.getType())) filtered.add(br.getName());
                        breedAdapter.clear(); breedAdapter.addAll(filtered); breedAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void loadPet(String id) {
        dbRef.child("pets").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                Pet p = snapshot.getValue(Pet.class);
                if (p == null) { Toast.makeText(EditPetActivity.this, "Pet not found", Toast.LENGTH_SHORT).show(); finish(); return; }
                etName.setText(p.getName());
                etPrice.setText(String.valueOf(p.getPrice()));
                selectedDob = p.getDob() == null ? "" : p.getDob();
                tvDob.setText(selectedDob.isEmpty() ? "DOB" : "DOB: " + selectedDob);
                // gender
                int gpos = genders.indexOf(p.getGender()); if (gpos >= 0) spinnerGender.setSelection(gpos);
                // type and breed selection will wait until types/breeds loaded; set via listeners after short delay by selecting if present
                // attempt to select type when types array changes by reloading types first (above)
                // We'll set selections after a brief post to UI thread to allow adapters to refresh
                spinnerType.post(() -> {
                    int tpos = types.indexOf(p.getType()); if (tpos >= 0) spinnerType.setSelection(tpos);
                    // populate breeds for this type then select breed
                    List<String> filtered = new ArrayList<>();
                    for (Breed b : breeds) if (p.getType() != null && p.getType().equals(b.getType())) filtered.add(b.getName());
                    breedAdapter.clear(); breedAdapter.addAll(filtered); breedAdapter.notifyDataSetChanged();
                    int bpos = filtered.indexOf(p.getBreed()); if (bpos >= 0) spinnerBreed.setSelection(bpos);
                });
            }
            @Override public void onCancelled(DatabaseError error) { Toast.makeText(EditPetActivity.this, "Failed to load pet", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR); int m = c.get(Calendar.MONTH); int d = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDob = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvDob.setText("DOB: " + selectedDob);
        }, y, m, d);
        dp.show();
    }

    private void updatePet() {
        String name = etName.getText().toString().trim();
        String gender = (String) spinnerGender.getSelectedItem();
        String type = types.isEmpty() ? "" : (String) spinnerType.getSelectedItem();
        String breed = (spinnerBreed.getSelectedItem() == null) ? "" : spinnerBreed.getSelectedItem().toString();
        String priceStr = etPrice.getText().toString().trim();
        if (name.isEmpty()) { Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show(); return; }
        double price = 0.0; try { if (!priceStr.isEmpty()) price = Double.parseDouble(priceStr); } catch (NumberFormatException e) { Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show(); return; }
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("gender", gender);
        data.put("type", type);
        data.put("breed", breed);
        data.put("price", price);
        data.put("dob", selectedDob);
        data.put("imageUrl", "");
        dbRef.child("pets").child(petId).setValue(data).addOnSuccessListener(aVoid -> {
            Toast.makeText(EditPetActivity.this, "Updated", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(EditPetActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
    }

    private void deletePet() {
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Delete this pet?")
                .setPositiveButton("Delete", (dialog, which) -> dbRef.child("pets").child(petId).removeValue().addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditPetActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(EditPetActivity.this, "Delete failed", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }
}

