package com.example.finalapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class AddPetActivity extends AppCompatActivity {

    private EditText etName, etPrice;
    private Spinner spinnerGender, spinnerType, spinnerBreed;
    private TextView tvDob;
    private Button btnPickDob, btnSave;

    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    private List<String> genders = new ArrayList<>();
    private List<String> types = new ArrayList<>();
    private List<Breed> breeds = new ArrayList<>();

    private ArrayAdapter<String> genderAdapter;
    private ArrayAdapter<String> typeAdapter;
    private ArrayAdapter<String> breedAdapter;

    private String selectedDob = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerBreed = findViewById(R.id.spinnerBreed);
        tvDob = findViewById(R.id.tvDob);
        btnPickDob = findViewById(R.id.btnPickDob);
        btnSave = findViewById(R.id.btnSavePet);

        genders.add("Male");
        genders.add("Female");
        genders.add("Unknown");
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
                // filter breeds by type
                List<String> filtered = new ArrayList<>();
                for (Breed b : breeds) {
                    if (selectedType.equals(b.getType())) filtered.add(b.getName());
                }
                breedAdapter.clear();
                breedAdapter.addAll(filtered);
                breedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        btnPickDob.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> savePet());
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
                if (types.isEmpty()) {
                    types.add("Dog");
                    types.add("Cat");
                    types.add("Bird");
                }
                typeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void loadBreeds() {
        dbRef.child("breeds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                breeds.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Breed b = child.getValue(Breed.class);
                    if (b != null) {
                        b.setId(child.getKey());
                        breeds.add(b);
                    }
                }
                // If type already selected, trigger filter
                if (!types.isEmpty() && spinnerType.getSelectedItemPosition() >= 0) {
                    int pos = spinnerType.getSelectedItemPosition();
                    if (pos < types.size()) {
                        String sel = types.get(pos);
                        List<String> filtered = new ArrayList<>();
                        for (Breed br : breeds) if (sel.equals(br.getType())) filtered.add(br.getName());
                        breedAdapter.clear();
                        breedAdapter.addAll(filtered);
                        breedAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDob = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvDob.setText("DOB: " + selectedDob);
        }, y, m, d);
        dp.show();
    }

    private void savePet() {
        String name = etName.getText().toString().trim();
        String gender = (String) spinnerGender.getSelectedItem();
        String type = types.isEmpty() ? "" : (String) spinnerType.getSelectedItem();
        String breed = (spinnerBreed.getSelectedItem() == null) ? "" : spinnerBreed.getSelectedItem().toString();
        String priceStr = etPrice.getText().toString().trim();

        if (name.isEmpty()) { Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show(); return; }
        double price = 0.0;
        try { if (!priceStr.isEmpty()) price = Double.parseDouble(priceStr); } catch (NumberFormatException e) { Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show(); return; }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("gender", gender);
        data.put("type", type);
        data.put("breed", breed);
        data.put("price", price);
        data.put("dob", selectedDob);
        data.put("imageUrl", "");

        dbRef.child("pets").push().setValue(data).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Pet saved", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show());
    }
}
