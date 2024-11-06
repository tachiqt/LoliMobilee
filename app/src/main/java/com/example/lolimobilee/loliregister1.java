package com.example.lolimobilee;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class loliregister1 extends AppCompatActivity {
    SimpleDateFormat dateFormat;
    private EditText birthday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loliregister1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText fullname = findViewById(R.id.editfullname);
        Spinner genderSpinner = findViewById(R.id.genderSpinner);
        birthday = findViewById(R.id.editbirthday);
        EditText email = findViewById(R.id.editemail);
        EditText password = findViewById(R.id.editpassword);
        EditText confirmPassword = findViewById(R.id.editconfirmpassword);
        Button nextButton = findViewById(R.id.btnnext);
        Button cancelButton = findViewById(R.id.btncancel);
        TextView alaccount = findViewById(R.id.loginnav);
        dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        birthday.setHint("Birthday (MM/DD/YY)");
        birthday.setOnClickListener(v -> showDatePickerDialog());

        alaccount.setOnClickListener(v -> {
            Intent intent = new Intent(loliregister1.this, login.class);
            startActivity(intent);
        });

        nextButton.setOnClickListener(v -> {
            String fullNameText = fullname.getText().toString();
            String gender = genderSpinner.getSelectedItem().toString();
            String birthdayText = birthday.getText().toString();
            String emailText = email.getText().toString();
            String passwordText = password.getText().toString();
            String confirmPasswordText = confirmPassword.getText().toString();

            if (fullNameText.isEmpty() || birthdayText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidEmail(emailText)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!passwordText.equals(confirmPasswordText)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidPassword(passwordText)) {
                Toast.makeText(this, "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one digit", Toast.LENGTH_LONG).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("email", emailText)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {// Email is not taken, proceed with registration
                                Intent intent = new Intent(loliregister1.this, register2.class);
                                // ... (putExtra remains the same) ...
                                startActivity(intent);
                            } else {
                                // Email is already taken, show error message
                                Toast.makeText(loliregister1.this, "Email is already taken", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Error occurred while checking email, show error message
                            Toast.makeText(loliregister1.this, "Error checking email", Toast.LENGTH_SHORT).show();
                        }
                    });

            Intent intent = new Intent(loliregister1.this, register2.class);
            intent.putExtra("fullName", fullNameText);
            intent.putExtra("gender", gender);
            intent.putExtra("birthday", birthdayText);
            intent.putExtra("email", emailText);
            intent.putExtra("password", passwordText);
        });


        cancelButton.setOnClickListener(v -> {
            showConfirmationDialog();
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(Calendar.YEAR, year1);
                    selectedDate.set(Calendar.MONTH, monthOfYear);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String formattedDate = dateFormat.format(selectedDate.getTime());
                    birthday.setText(formattedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to go back to the welcome page?")
                .setPositiveButton("OK", (dialog, which) -> {

                    EditText fullname= findViewById(R.id.editfullname);
                    Spinner genderSpinner = findViewById(R.id.genderSpinner);
                    EditText birthday = findViewById(R.id.editbirthday);
                    EditText email = findViewById(R.id.editemail);
                    EditText password = findViewById(R.id.editpassword);
                    EditText confirmPassword = findViewById(R.id.editconfirmpassword);

                    fullname.setText("");
                    genderSpinner.setSelection(0); // Reset spinner to default selection
                    birthday.setText("");
                    email.setText("");
                    password.setText("");
                    confirmPassword.setText("");

                    // Navigate back to the main activity (welcome page)
                    Intent intent = new Intent(loliregister1.this, MainActivity.class); // Assuming MainActivity is your welcome page
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the back stack
                    startActivity(intent);
                    finish(); // Finish the current activity
                })
                .setNegativeButton("Cancel", null) // Do nothing if "Cancel" is clicked
                .show();
    }
        private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$").matcher(password).matches();
    }
}
