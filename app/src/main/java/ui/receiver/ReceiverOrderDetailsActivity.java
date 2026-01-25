package ui.receiver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.namah.feedwithlove.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReceiverOrderDetailsActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 101;

    private String foodId;
    private String receiverEmail;
    private DatabaseReference foodRef;

    private double lastLatitude = 0.0;
    private double lastLongitude = 0.0;


    private EditText etDropLocation, etContact, etInstructions;
    private FusedLocationProviderClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        setContentView(R.layout.activity_receiver_order_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        // 🔗 Views
        etDropLocation = findViewById(R.id.etDropLocation);
        etContact = findViewById(R.id.etContact);
        etInstructions = findViewById(R.id.etInstructions);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // 🔗 Food ID
        foodId = getIntent().getStringExtra("food_id");
        if (foodId == null) {
            Toast.makeText(this, "Invalid food item", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        foodRef = FirebaseDatabase.getInstance()
                .getReference("foods")
                .child(foodId);

        fetchReceiverEmail();

        findViewById(R.id.btnFinalizeOrder).setOnClickListener(v -> finalizeOrder());

        // 📍 Auto fetch location
        etDropLocation.setOnClickListener(v -> fetchCurrentLocation());
    }

    /* ================= FETCH RECEIVER EMAIL ================= */

    private void fetchReceiverEmail() {

        if (!AWSMobileClient.getInstance().isSignedIn()) return;

        AWSMobileClient.getInstance().getUserAttributes(
                new Callback<Map<String, String>>() {
                    @Override
                    public void onResult(Map<String, String> attributes) {
                        receiverEmail = attributes.get("email");
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(ReceiverOrderDetailsActivity.this,
                                "Failed to load user email",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /* ================= LOCATION ================= */

    private void fetchCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE
            );
            return;
        }

        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {

                        lastLatitude = location.getLatitude();
                        lastLongitude = location.getLongitude();

                        convertToAddress(lastLatitude, lastLongitude);
                    } else {
                        Toast.makeText(this,
                                "Unable to get location",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void convertToAddress(double lat, double lng) {

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses != null && !addresses.isEmpty()) {
                etDropLocation.setText(addresses.get(0).getAddressLine(0));
            } else {
                Toast.makeText(this,
                        "Address not found",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this,
                    "Geocoder failed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /* ================= PERMISSION RESULT ================= */

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            fetchCurrentLocation();
        }
    }

    /* ================= UPDATE FIREBASE ================= */

    private void finalizeOrder() {

        if (receiverEmail == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String dropAddress = etDropLocation.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String instructions = etInstructions.getText().toString().trim();

        if (dropAddress.isEmpty()) {
            etDropLocation.setError("Drop location required");
            return;
        }

        if (contact.isEmpty()) {
            etContact.setError("Contact number required");
            return;
        }

        // 🔹 Prepare data map (IMPORTANT PART)
        Map<String, Object> data = new HashMap<>();

        // ✅ Drop location (as you requested)
        data.put("location/drop/address", dropAddress);
        data.put("location/drop/latitude", lastLatitude);     // double
        data.put("location/drop/longitude", lastLongitude);   // double

        // ✅ Info
        data.put("info/contact", contact);
        data.put("info/etInstructions",
                instructions.isEmpty() ? "None" : instructions);

        // ✅ Receiver & status
        data.put("role/receiver", receiverEmail);
        data.put("status/state", "UNAVAILABLE");
        data.put("status/delivery", "PENDING");

        // ✅ Timestamp
        data.put("timestamps/updatedAt", System.currentTimeMillis());

        // 🔥 SINGLE FIREBASE UPDATE
        foodRef.updateChildren(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Order Confirmed! Location saved.",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

}
