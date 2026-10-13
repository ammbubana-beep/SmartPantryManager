package com.aifani.smartpantrymanager.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Saves the user's settings on the device (SharedPreferences). */
public final class SettingsStore {

    private static final String PREFS_NAME = "smart_pantry_prefs";
    private static final String KEY_HIGHLIGHT_EXPIRING = "highlight_expiring";

    private SettingsStore() { }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Defaults to true: expiring items are highlighted unless the user turns it off. */
    public static boolean isHighlightExpiring(Context context) {
        return prefs(context).getBoolean(KEY_HIGHLIGHT_EXPIRING, true);
    }

    public static void setHighlightExpiring(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_HIGHLIGHT_EXPIRING, enabled).apply();
    }
}