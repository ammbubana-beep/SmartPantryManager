package com.aifani.smartpantrymanager.util;

import java.util.Locale;

/** Converts user units into base units: g (weight), ml (volume), pcs (count). */
public final class UnitConverter {

    private UnitConverter() { }

    public static String toBaseUnit(String unit) {
        switch (clean(unit)) {
            case "g":
            case "kg":
                return "g";
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return "ml";
            default:
                return "pcs";
        }
    }

    public static double toBaseQuantity(double quantity, String unit) {
        switch (clean(unit)) {
            case "kg":   return quantity * 1000;
            case "l":    return quantity * 1000;
            case "tsp":  return quantity * 5;
            case "tbsp": return quantity * 15;
            case "cup":  return quantity * 250;
            default:     return quantity;
        }
    }

    private static String clean(String unit) {
        return unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
    }
}