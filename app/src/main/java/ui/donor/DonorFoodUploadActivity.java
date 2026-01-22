package ui.donor;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.namah.feedwithlove.R;

import java.util.Calendar;

public class DonorFoodUploadActivity extends AppCompatActivity {

    private ImageView ivFoodImage;
    private LinearLayout layoutUploadPlaceholder;
    private TextInputEditText etTime, etFoodName, etQuantity, etLocation, etNotes;
    private MaterialButton btnCapture, btnGallery, btnSubmit;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    displayImage(imageBitmap);
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivFoodImage.setImageURI(uri);
                    ivFoodImage.setVisibility(View.VISIBLE);
                    layoutUploadPlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable fullscreen / edge-to-edge
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );
        
        setContentView(R.layout.activity_donor_food_upload);

        // Handle Window Insets to prevent overlapping with status bar and navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_donor_upload), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        ivFoodImage = findViewById(R.id.ivFoodImage);
        layoutUploadPlaceholder = findViewById(R.id.layoutUploadPlaceholder);
        etTime = findViewById(R.id.etTime);
        etFoodName = findViewById(R.id.etFoodName);
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);
        etNotes = findViewById(R.id.etNotes);
        btnCapture = findViewById(R.id.btnCapture);
        btnGallery = findViewById(R.id.btnGallery);
        btnSubmit = findViewById(R.id.btnSubmit);
        
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        btnCapture.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(takePictureIntent);
        });

        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        etTime.setOnClickListener(v -> showTimePicker());

        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                Toast.makeText(this, "Donation submitted successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showTimePicker() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> 
            etTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute)), hour, minute, true);
        mTimePicker.setTitle("Select Pickup Time");
        mTimePicker.show();
    }

    private void displayImage(Bitmap bitmap) {
        ivFoodImage.setImageBitmap(bitmap);
        ivFoodImage.setVisibility(View.VISIBLE);
        layoutUploadPlaceholder.setVisibility(View.GONE);
    }

    private boolean validateForm() {
        if (etFoodName.getText().toString().trim().isEmpty()) {
            etFoodName.setError("Required");
            return false;
        }
        if (etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Required");
            return false;
        }
        return true;
    }
}