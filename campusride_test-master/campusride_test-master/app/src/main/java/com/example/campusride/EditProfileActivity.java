package com.example.campusride;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName, editRegisterNo, editRole, editGender;
    private Button btnSave;
    private DatabaseReference userRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Views
        editName = findViewById(R.id.editName);
        editRegisterNo = findViewById(R.id.editRegisterNo);
        editRole = findViewById(R.id.editRole);
        editGender = findViewById(R.id.editGender);
        btnSave = findViewById(R.id.btnSave);

        // Get current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        }

        // Save Updated Data
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void saveProfileChanges() {
        String newName = editName.getText().toString().trim();
        String newRegisterNo = editRegisterNo.getText().toString().trim();
        String newRole = editRole.getText().toString().trim();
        String newGender = editGender.getText().toString().trim();

        if (newName.isEmpty() || newRegisterNo.isEmpty() || newRole.isEmpty() || newGender.isEmpty()) {
            Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("registerNo", newRegisterNo);
        updates.put("role", newRole);
        updates.put("gender", newGender);

        userRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            finish(); // Close EditProfileActivity
        }).addOnFailureListener(e -> Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show());
    }
}
