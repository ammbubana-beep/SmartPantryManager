package com.aifani.smartpantrymanager.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One ingredient the user currently has at home.
 * Room turns this class into the "pantry_items" table.
 */
@Entity(tableName = "pantry_items")
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    /** Name as the user typed it, e.g. "Tomatoes". Shown in the list. */
    public String name;

    /** Cleaned-up name used for matching, e.g. "tomato". */
    public String normalizedName;

    /** Amount the user has, e.g. 500. */
    public double quantity;

    /** Unit the user chose, e.g. "g", "kg", "ml", "l", "pcs". */
    public String unit;

    /** Optional expiry date as milliseconds since 1970; null if not set. */
    public Long expiryDate;
}