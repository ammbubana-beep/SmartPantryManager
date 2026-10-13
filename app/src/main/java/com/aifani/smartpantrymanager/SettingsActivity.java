package com.aifani.smartpantrymanager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aifani.smartpantrymanager.util.NavigationHelper;
import com.aifani.smartpantrymanager.util.SettingsStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

/** Settings screen: lets the user turn the expiring-soon highlight on or off. */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settingsRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        SwitchMaterial switchExpiring = findViewById(R.id.switchExpiring);
        switchExpiring.setChecked(SettingsStore.isHighlightExpiring(this));
        switchExpiring.setOnCheckedChangeListener((button, isChecked) ->
                SettingsStore.setHighlightExpiring(this, isChecked));

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        NavigationHelper.setup(this, nav, R.id.nav_settings);
    }
}