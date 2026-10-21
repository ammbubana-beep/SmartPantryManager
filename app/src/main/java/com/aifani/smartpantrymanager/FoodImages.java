package com.aifani.smartpantrymanager;

import android.content.Context;
import java.util.Locale;

public final class FoodImages {

    private FoodImages() {}

    public static int find(Context context, String prefix, String name) {
        if (name == null) return 0;
        String key = prefix + name.toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return context.getResources()
                .getIdentifier(key, "drawable", context.getPackageName());
    }
}