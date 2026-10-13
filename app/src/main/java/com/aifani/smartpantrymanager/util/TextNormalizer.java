package com.aifani.smartpantrymanager.util;

import java.util.Locale;

/** Turns what the user typed into a standard form used for matching. */
public final class TextNormalizer {

    private TextNormalizer() { }

    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");

        if (s.length() > 3) {
            if (s.endsWith("ies")) {                       // berries -> berry
                s = s.substring(0, s.length() - 3) + "y";
            } else if (s.endsWith("oes")) {                // tomatoes -> tomato
                s = s.substring(0, s.length() - 2);
            } else if (s.endsWith("ches") || s.endsWith("shes")) {  // peaches -> peach
                s = s.substring(0, s.length() - 2);
            } else if (s.endsWith("s") && !s.endsWith("ss") && !s.endsWith("us")) {
                s = s.substring(0, s.length() - 1);        // eggs -> egg
            }
        }
        return s;
    }
}