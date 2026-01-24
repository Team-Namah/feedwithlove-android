package ui.volunteer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.namah.feedwithlove.R;

public class VolunteerDeliveryActionActivity extends AppCompatActivity {

    private ImageView ivDeliveryProof;
    private View layoutPlaceholder;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ivDeliveryProof.setImageBitmap(imageBitmap);
                    ivDeliveryProof.setVisibility(View.VISIBLE);
                    layoutPlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );
        setContentView(R.layout.activity_volunteer_delivery_action);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivDeliveryProof = findViewById(R.id.ivDeliveryProof);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        findViewById(R.id.cardCaptureProof).setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(takePictureIntent);
        });

        findViewById(R.id.btnSubmitProof).setOnClickListener(v -> {
            if (ivDeliveryProof.getVisibility() == View.VISIBLE) {
                Toast.makeText(this, "Delivery Completed Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Please capture a photo first", Toast.LENGTH_SHORT).show();
            }
        });
    }
}