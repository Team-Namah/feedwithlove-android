package ui.volunteer;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.namah.feedwithlove.CognitoManager;
import com.namah.feedwithlove.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class EditVolunteerProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail;
    private ImageView ivEditAvatar;

    // ✅ MUST MATCH YOUR REAL BUCKET
    private static final String BUCKET_NAME = "feed-profile-images";
    private static final Regions REGION = Regions.AP_SOUTH_1;

    private String profileImageUrl;
    private Uri selectedImageUri;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_donor_profile);

        EdgeToEdge.enable(
                this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        ivEditAvatar = findViewById(R.id.ivEditAvatar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageToS3(selectedImageUri);
            } else {
                saveProfileTextOnly();
            }
        });

        findViewById(R.id.clAvatarContainer).setOnClickListener(v -> openImagePicker());

        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                selectedImageUri = uri;
                                Picasso.get()
                                        .load(uri)
                                        .placeholder(R.drawable.bg_splash)
                                        .error(R.drawable.bg_splash)
                                        .fit()
                                        .centerCrop()
                                        .into(ivEditAvatar);
                            }
                        }
                );

        loadCurrentDetails();
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void loadCurrentDetails() {
        CognitoUserPool userPool = CognitoManager.getUserPool(this);
        CognitoUser user = userPool.getCurrentUser();
        if (user == null) return;

        user.getDetailsInBackground(new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails details) {
                Map<String, String> attrs = details.getAttributes().getAttributes();
                runOnUiThread(() -> {
                    etName.setText(attrs.get("name"));
                    etEmail.setText(attrs.get("email"));
                    profileImageUrl = attrs.get("picture");
                    loadProfileImage(profileImageUrl);
                });
            }

            @Override
            public void onFailure(Exception exception) {
                runOnUiThread(() ->
                        Toast.makeText(EditVolunteerProfileActivity.this,
                                "Failed to load profile", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadProfileImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            ivEditAvatar.setImageResource(R.drawable.bg_splash);
            return;
        }

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.bg_splash)
                .error(R.drawable.bg_splash)
                .fit()
                .centerCrop()
                .into(ivEditAvatar);
    }

    private void uploadImageToS3(Uri imageUri) {

        if (!AWSMobileClient.getInstance().isSignedIn()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        File cachedFile = createTempFileFromUri(imageUri);
        if (cachedFile == null) return;

        AmazonS3Client s3Client =
                new AmazonS3Client(AWSMobileClient.getInstance());

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .s3Client(s3Client)
                        .build();

        String key = "profiles/profile_" + System.currentTimeMillis() + ".jpg";

        TransferObserver observer =
                transferUtility.upload(BUCKET_NAME, key, cachedFile);

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {

                    String s3Url =
                            "https://" + BUCKET_NAME +
                                    ".s3." + REGION.getName() +
                                    ".amazonaws.com/" + key;

                    updateProfilePictureAttribute(s3Url);
                    cachedFile.delete();
                }
            }

            @Override public void onProgressChanged(int id, long c, long t) {}

            @Override
            public void onError(int id, Exception ex) {
                runOnUiThread(() ->
                        Toast.makeText(EditVolunteerProfileActivity.this,
                                "Upload failed", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateProfilePictureAttribute(String url) {
        CognitoUser user =
                CognitoManager.getUserPool(this).getCurrentUser();
        if (user == null) return;

        CognitoUserAttributes attrs = new CognitoUserAttributes();
        attrs.addAttribute("picture", url);

        user.updateAttributesInBackground(attrs, new UpdateAttributesHandler() {
            @Override
            public void onSuccess(List list) {
                runOnUiThread(() -> {
                    selectedImageUri = null;
                    saveProfileTextOnly();
                });
            }

            @Override
            public void onFailure(Exception exception) {
                runOnUiThread(() ->
                        Toast.makeText(EditVolunteerProfileActivity.this,
                                "Failed to update picture", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveProfileTextOnly() {
        hideKeyboard();

        CognitoUser user =
                CognitoManager.getUserPool(this).getCurrentUser();
        if (user == null) return;

        CognitoUserAttributes attrs = new CognitoUserAttributes();
        attrs.addAttribute("name", etName.getText().toString());
        attrs.addAttribute("email", etEmail.getText().toString());

        user.updateAttributesInBackground(attrs, new UpdateAttributesHandler() {
            @Override
            public void onSuccess(List list) {
                runOnUiThread(() -> {
                    Toast.makeText(EditVolunteerProfileActivity.this,
                            "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(Exception exception) {
                runOnUiThread(() ->
                        Toast.makeText(EditVolunteerProfileActivity.this,
                                "Update failed", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "profile_temp.jpg");
            FileOutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
