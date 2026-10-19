package com.aifani.smartpantrymanager.util;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
public final class RecipeImages {

    private static final String DEFAULT_ICON = "\uD83C\uDF7D\uFE0F";
    private static final Map<String, String> ICONS = new LinkedHashMap<>();

    static {
        ICONS.put("chicken", "🍗");
        ICONS.put("rice", "🍚");
        ICONS.put("pasta", "🍝");
        ICONS.put("macaroni", "🧀");
        ICONS.put("pancake", "🥞");
        ICONS.put("soup", "🍲");
        ICONS.put("porridge", "🥣");
        ICONS.put("stir-fry", "🥘");
        ICONS.put("potato", "🥔");
        ICONS.put("omelette", "🍳");
        ICONS.put("egg", "🍳");
        ICONS.put("bean", "🥫");
        ICONS.put("sandwich", "🥪");
        ICONS.put("toastie", "🥪");
        ICONS.put("bread", "🍞");
        ICONS.put("toast", "🍞");
    }

    private RecipeImages() { }
    public static String drawableName(String recipeName) {
        String slug = recipeName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return "recipe_" + slug;
    }

    public static String emojiFor(String recipeName) {
        String lower = recipeName.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : ICONS.entrySet()) {
            if (lower.contains(entry.getKey())) return entry.getValue();
        }
        return DEFAULT_ICON;
    }

    public static void apply(Context context, String recipeName,
                             ImageView image, TextView emoji) {
        int resId = context.getResources().getIdentifier(
                drawableName(recipeName), "drawable", context.getPackageName());
        if (resId != 0) {
            image.setImageResource(resId);
            image.setVisibility(View.VISIBLE);
            emoji.setVisibility(View.GONE);
        } else {
            image.setVisibility(View.GONE);
            emoji.setText(emojiFor(recipeName));
            emoji.setVisibility(View.VISIBLE);
        }
    }
}