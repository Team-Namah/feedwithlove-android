package ui.donor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.namah.feedwithlove.R;

public class FragmentDonorHome extends Fragment {

    public FragmentDonorHome() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donor_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.cardDonate).setOnClickListener(v -> startActivity(new Intent(requireContext(), DonorFoodUploadActivity.class)));
        view.findViewById(R.id.btnDonate).setOnClickListener(v -> startActivity(new Intent(requireContext(), DonorFoodUploadActivity.class)));
    }
}