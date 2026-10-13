package com.aifani.smartpantrymanager.util;

import android.app.Activity;
import android.content.Intent;

import com.aifani.smartpantrymanager.MainActivity;
import com.aifani.smartpantrymanager.R;
import com.aifani.smartpantrymanager.SettingsActivity;
import com.aifani.smartpantrymanager.SuggestedRecipesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Connects the bottom navigation bar to the three main screens using Intents. */
public final class NavigationHelper {

    private NavigationHelper() { }

    public static void setup(Activity activity, BottomNavigationView nav, int selectedId) {
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) return true;   // already on this screen

            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = MainActivity.class;
            } else if (id == R.id.nav_recipes) {
                target = SuggestedRecipesActivity.class;
            } else {
                target = SettingsActivity.class;
            }

            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);

            // Keep the back stack short: only the Pantry screen stays underneath.
            if (!(activity instanceof MainActivity)) {
                activity.finish();
            }
            return true;
        });
    }
}