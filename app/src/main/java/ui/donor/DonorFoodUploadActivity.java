package ui.donor;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.namah.feedwithlove.FileUtils;
import com.namah.feedwithlove.R;
import com.namah.feedwithlove.Status;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.security.auth.callback.Callback;

public class DonorFoodUploadActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE = 102;

    private String userEmail = null;
    private String userRole = null;

    private double currentLat = 0.0;
    private double currentLng = 0.0;

    private ImageView ivFoodImage;
    private LinearLayout layoutUploadPlaceholder;
    private TextInputEditText etTime, etFoodName, etQuantity, etLocation, etNotes;
    private MaterialButton btnGallery, btnSubmit, btnCapture;

    private Uri imageUri;
    private DatabaseReference rootRef;
    private FusedLocationProviderClient fusedLocationClient;

    // 🔴 AWS DETAILS (TESTING ONLY)
    private static final String ACCESS_KEY = "AKIA4RJENHKGARX4UHX4";
    private static final String SECRET_KEY = "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";
    private static final String BUCKET_NAME = "tts-image-upload";

    /* ---------------- GALLERY ---------------- */
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivFoodImage.setImageURI(uri);
                    ivFoodImage.setVisibility(View.VISIBLE);
                    layoutUploadPlaceholder.setVisibility(View.GONE);
                }
            });

    /* ---------------- CAMERA ---------------- */
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    imageUri = saveBitmap(bitmap);
                    ivFoodImage.setImageBitmap(bitmap);
                    ivFoodImage.setVisibility(View.VISIBLE);
                    layoutUploadPlaceholder.setVisibility(View.GONE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));

        setContentView(R.layout.activity_donor_food_upload);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_donor_upload), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        rootRef = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initViews();
        setupListeners();
        loadUserDetails();

    }


    private void loadUserDetails() {

        if (!AWSMobileClient.getInstance().isSignedIn()) return;

        AWSMobileClient.getInstance().getUserAttributes(
                new com.amazonaws.mobile.client.Callback<Map<String, String>>() {
                    @Override
                    public void onResult(Map<String, String> attributes) {

                        runOnUiThread(() -> {
                            userEmail = attributes.get("email");
                            userRole  = attributes.get("custom:role");
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() ->
                            Toast.makeText(DonorFoodUploadActivity.this,
                                    "Failed to load user data",
                                    Toast.LENGTH_SHORT).show()
                        );
                    }
                }
        );
    }

    private void initViews() {
        ivFoodImage = findViewById(R.id.ivFoodImage);
        layoutUploadPlaceholder = findViewById(R.id.layoutUploadPlaceholder);
        etFoodName = findViewById(R.id.etFoodName);
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);
        etTime = findViewById(R.id.etTime);
        etNotes = findViewById(R.id.etNotes);
        btnGallery = findViewById(R.id.btnGallery);
        btnCapture = findViewById(R.id.btnCapture);
        btnSubmit = findViewById(R.id.btnSubmit);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnCapture.setOnClickListener(v -> openCamera());
        etLocation.setOnClickListener(v -> fetchLocation());
        etTime.setOnClickListener(v -> showTimePicker());
        btnSubmit.setOnClickListener(v -> {
            if (validate()) createFoodEntry();
        });
    }

    /* ---------------- CAMERA ---------------- */
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            cameraLauncher.launch(null);
        }
    }

    private Uri saveBitmap(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(),
                    "camera_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return Uri.fromFile(file);
        } catch (Exception e) {
            return null;
        }
    }
    private File getFileFromUri(Uri uri) {
        try {
            File file = new File(getCacheDir(),
                    "upload_" + System.currentTimeMillis() + ".jpg");

            try (java.io.InputStream in = getContentResolver().openInputStream(uri);
                 java.io.OutputStream out = new java.io.FileOutputStream(file)) {

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    /* ---------------- LOCATION ---------------- */
    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                new Thread(() -> {
                    try {
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        List<Address> list =
                                geocoder.getFromLocation(currentLat, currentLng, 1);
                        if (!list.isEmpty()) {
                            String address = list.get(0).getAddressLine(0);
                            runOnUiThread(() -> etLocation.setText(address));
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> toast("Unable to fetch address"));
                    }
                }).start();
            }
        });
    }

    /* ---------------- TIME ---------------- */
    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this,
                (v, h, m) -> etTime.setText(String.format("%02d:%02d", h, m)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true).show();
    }

    /* ---------------- VALIDATION ---------------- */
    private boolean validate() {
        if (imageUri == null) return toastFalse("Select image");
        if (etFoodName.getText().toString().trim().isEmpty())
            return error(etFoodName);
        if (etLocation.getText().toString().trim().isEmpty())
            return error(etLocation);
        return true;
    }



    /* ---------------- FIREBASE + AWS ---------------- */
    private void createFoodEntry() {

        String foodId = rootRef.child("foods").push().getKey();
        long now = System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();

        data.put("basic/title", etFoodName.getText().toString());
        data.put("basic/quantity", etQuantity.getText().toString());
        data.put("basic/expiryTime", etTime.getText().toString());

        data.put("location/pickup/address", etLocation.getText().toString());
        data.put("location/pickup/latitude", currentLat);
        data.put("location/pickup/longitude", currentLng);

        data.put("location/drop/address", Status.NULL.name());
        data.put("location/drop/latitude", Status.NULL.name());
        data.put("location/drop/longitude", Status.NULL.name());


        data.put("status/state", Status.AVAILABLE.name());
        data.put("status/delivery", Status.PENDING.name());
        data.put("status/delivery_valounteer", Status.NULL.name());
        data.put("timestamps/createdAt", now);
        data.put("timestamps/updatedAt", now);


        data.put("role/volunteer", Status.NULL.name());
        data.put("role/receiver", Status.NULL.name());
        data.put("role/donor", Status.NULL.name());

        if (userRole != null && userEmail != null) {

            switch (userRole.toLowerCase()) {
                case "donor":
                    data.put("role/donor", userEmail);
                    break;

                case "volunteer":
                    data.put("role/volunteer", userEmail);
                    break;

                case "receiver":
                    data.put("role/receiver", userEmail);
                    break;
            }
        }
        rootRef.child("foods").child(foodId).updateChildren(data);

        uploadImageToAWS(foodId);
    }

    private void uploadImageToAWS(String foodId) {

        File file = getFileFromUri(imageUri);
        if (file == null) {
            toast("Image processing failed");
            return;
        }

        BasicAWSCredentials credentials =
                new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);

        AmazonS3Client s3Client = new AmazonS3Client(credentials);
        s3Client.setRegion(Region.getRegion(Regions.AP_SOUTH_1));

        TransferNetworkLossHandler.getInstance(this);

        TransferUtility transferUtility = TransferUtility.builder()
                .context(this)
                .s3Client(s3Client)
                .build();

        String fileName = foodId + "_" + UUID.randomUUID() + ".jpg";

        TransferObserver observer =
                transferUtility.upload(BUCKET_NAME, fileName, file);

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {

                    String imageUrl =
                            "https://" + BUCKET_NAME +
                                    ".s3.ap-south-1.amazonaws.com/" +
                                    fileName;

                    rootRef.child("foods")
                            .child(foodId)
                            .child("basic")
                            .child("imageUrl")
                            .setValue(imageUrl);

                    runOnUiThread(() -> {
                        toast("Food uploaded successfully");
                        finish();
                    });
                }
            }

            @Override public void onProgressChanged(int id, long b, long t) {}
            @Override public void onError(int id, Exception ex) {
                runOnUiThread(() -> toast(ex.getMessage()));
            }
        });
    }


    /* ---------------- HELPERS ---------------- */
    private boolean toastFalse(String m) { toast(m); return false; }
    private boolean error(TextInputEditText e) { e.setError("Required"); return false; }
    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    /* ---------------- PERMISSIONS ---------------- */
    @Override
    public void onRequestPermissionsResult(
            int code, @NonNull String[] p, @NonNull int[] r) {
        super.onRequestPermissionsResult(code, p, r);
        if (code == CAMERA_PERMISSION_CODE && r.length > 0 &&
                r[0] == PackageManager.PERMISSION_GRANTED)
            openCamera();
        if (code == LOCATION_PERMISSION_CODE && r.length > 0 &&
                r[0] == PackageManager.PERMISSION_GRANTED)
            fetchLocation();
    }
}
