package ui.volunteer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.namah.feedwithlove.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.UUID;

public class VolunteerDeliveryActionActivity extends AppCompatActivity {

    private ImageView ivDeliveryProof;
    private View layoutPlaceholder;

    private Uri imageUri;
    private String userEmail;

    private DatabaseReference foodsRef;

    // 🔴 AWS DETAILS (SAME AS DONOR)
    private static final String ACCESS_KEY = "AKIA4RJENHKGARX4UHX4";
    private static final String SECRET_KEY = "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";
    private static final String BUCKET_NAME = "tts-image-upload";

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();
                            Bitmap bitmap = (Bitmap) extras.get("data");
                            imageUri = saveBitmap(bitmap);
                            ivDeliveryProof.setImageBitmap(bitmap);
                            ivDeliveryProof.setVisibility(View.VISIBLE);
                            layoutPlaceholder.setVisibility(View.GONE);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));

        setContentView(R.layout.activity_volunteer_delivery_action);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content),
                (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        ivDeliveryProof = findViewById(R.id.ivDeliveryProof);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);

        foodsRef = FirebaseDatabase.getInstance().getReference("foods");

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        findViewById(R.id.cardCaptureProof).setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        });

        findViewById(R.id.btnSubmitProof).setOnClickListener(v -> {
            if (imageUri == null) {
                Toast.makeText(this, "Please capture a photo first", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadCompletedImage();
        });

        loadUserEmail();
    }

    /* ---------------- USER EMAIL ---------------- */
    private void loadUserEmail() {
        if (!AWSMobileClient.getInstance().isSignedIn()) return;

        AWSMobileClient.getInstance().getUserAttributes(
                new Callback<Map<String, String>>() {
                    @Override
                    public void onResult(Map<String, String> attributes) {
                        userEmail = attributes.get("email");
                    }

                    @Override
                    public void onError(Exception e) { }
                }
        );
    }

    /* ---------------- BITMAP TO FILE ---------------- */
    private Uri saveBitmap(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(),
                    "delivery_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return Uri.fromFile(file);
        } catch (Exception e) {
            return null;
        }
    }

    private File getFileFromUri(Uri uri) {
        return new File(uri.getPath());
    }

    /* ---------------- MAIN LOGIC ---------------- */
    private void uploadCompletedImage() {

        if (userEmail == null) return;

        foodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String volunteer =
                            snap.child("role/volunteer").getValue(String.class);

                    if (volunteer != null &&
                            volunteer.equalsIgnoreCase(userEmail)) {

                        uploadToAWSAndUpdateFirebase(snap.getKey());
                        return;
                    }
                }

                Toast.makeText(VolunteerDeliveryActionActivity.this,
                        "No assigned delivery found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    /* ---------------- AWS UPLOAD + FIREBASE UPDATE ---------------- */
    private void uploadToAWSAndUpdateFirebase(String foodId) {

        File file = getFileFromUri(imageUri);
        if (file == null) return;

        BasicAWSCredentials credentials =
                new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);

        AmazonS3Client s3Client = new AmazonS3Client(credentials);
        s3Client.setRegion(Region.getRegion(Regions.AP_SOUTH_1));

        TransferNetworkLossHandler.getInstance(this);

        TransferUtility transferUtility = TransferUtility.builder()
                .context(this)
                .s3Client(s3Client)
                .build();

        String fileName = "completed_" + foodId + "_" + UUID.randomUUID() + ".jpg";

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

                    DatabaseReference ref =
                            foodsRef.child(foodId);

                    ref.child("basic")
                            .child("completed_image")
                            .setValue(imageUrl);

                    ref.child("status")
                            .child("delivery")
                            .setValue("COMPLETED");

                    ref.child("status")
                            .child("delivery_valounteer")
                            .setValue("COMPLETED");

                    Toast.makeText(
                            VolunteerDeliveryActionActivity.this,
                            "Delivery Completed Successfully!",
                            Toast.LENGTH_SHORT).show();

                    finish();
                }
            }

            @Override public void onProgressChanged(int id, long b, long t) { }
            @Override public void onError(int id, Exception ex) {
                Toast.makeText(
                        VolunteerDeliveryActionActivity.this,
                        ex.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
