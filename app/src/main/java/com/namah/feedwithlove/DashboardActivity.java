package com.namah.feedwithlove;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import me.ibrahimsn.lib.SmoothBottomBar;
import me.ibrahimsn.lib.OnItemSelectedListener;

public class DashboardActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private SmoothBottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);
        // Enable fullscreen / edge-to-edge
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        viewPager = findViewById(R.id.viewPager);
        bottomBar = findViewById(R.id.bottomBar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            // Get the system bar insets, specifically the bottom inset for the navigation bar
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply the bottom inset as a margin to the view to prevent overlap
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = systemBars.bottom;
            v.setLayoutParams(params);

            // Consume the insets to prevent them from being passed to child views
            return WindowInsetsCompat.CONSUMED;
        });

        viewPager.setAdapter(new DashboardPagerAdapter(this));

        // Disable user swiping
        viewPager.setUserInputEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }


        // Sync BottomBar with ViewPager
        bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                viewPager.setCurrentItem(i, false); // false for no smooth scroll on click
                return true;
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                bottomBar.setItemActiveIndex(position);

                // CHANGE BOTTOM BAR COLOR WHEN FRAGMENT CHANGES
                if (position == 2) {  // Profile Fragment
                    bottomBar.setBarBackgroundColor(
                            ContextCompat.getColor(DashboardActivity.this, R.color.white)
                    );
                } else {
                    bottomBar.setBarBackgroundColor(
                            ContextCompat.getColor(DashboardActivity.this, R.color.love_bg_warm)
                    );
                }
            }
        });
    }

    private static class DashboardPagerAdapter extends FragmentStateAdapter {

        public DashboardPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new FragmentHome();
                case 1:
                    return new FragmentStats();
                case 2:
                    return new FragmentProfile();
                default:
                    return new FragmentHome();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
