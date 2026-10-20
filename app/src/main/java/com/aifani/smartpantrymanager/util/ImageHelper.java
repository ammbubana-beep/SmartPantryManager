package com.aifani.smartpantrymanager.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aifani.smartpantrymanager.data.PantryItem;

import java.util.LinkedHashMap;
import java.util.Map;
public final class IngredientIcons {

    private static final String DEFAULT_ICON = "\uD83C\uDF7D\uFE0F";
    private static final Map<String, String> ICONS = new LinkedHashMap<>();

    static {
        ICONS.put("peanut butter", "🥜");
        ICONS.put("butter", "🧈");
        ICONS.put("egg", "🥚");
        ICONS.put("bread", "🍞");
        ICONS.put("cheese", "🧀");
        ICONS.put("milk", "🥛");
        ICONS.put("macaroni", "🍝");
        ICONS.put("pasta", "🍝");
        ICONS.put("rice", "🍚");
        ICONS.put("flour", "🌾");
        ICONS.put("lentil", "🥣");
        ICONS.put("oat", "🥣");
        ICONS.put("cabbage", "🥬");
        ICONS.put("tomato", "🍅");
        ICONS.put("potato", "🥔");
        ICONS.put("onion", "🧅");
        ICONS.put("garlic", "🧄");
        ICONS.put("carrot", "🥕");
        ICONS.put("banana", "🍌");
        ICONS.put("chicken", "🍗");
        ICONS.put("tuna", "🐟");
        ICONS.put("bean", "🥫");
        ICONS.put("soy sauce", "🍶");
        ICONS.put("oil", "🍾");
        ICONS.put("sugar", "🍬");
        ICONS.put("honey", "🍯");
        ICONS.put("stock", "🍲");
    }

    private IngredientIcons() { }

    public static String emojiFor(String name) {
        String normalized = TextNormalizer.normalizeName(name);
        for (Map.Entry<String, String> entry : ICONS.entrySet()) {
            if (normalized.contains(entry.getKey())) return entry.getValue();
        }
        return DEFAULT_ICON;
    }
    public static void apply(Context context, PantryItem item,
                             ImageView image, TextView emoji) {
        Bitmap photo = ImageHelper.load(item.imagePath);
        if (photo != null) {
            image.setImageBitmap(photo);
            image.setVisibility(View.VISIBLE);
            emoji.setVisibility(View.GONE);
        } else {
            image.setVisibility(View.GONE);
            emoji.setText(emojiFor(item.name));
            emoji.setVisibility(View.VISIBLE);
        }
    }
}