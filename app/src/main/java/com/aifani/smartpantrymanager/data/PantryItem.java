package com.aifani.smartpantrymanager.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pantry_items")
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public String normalizedName;

    public double quantity;

    public String unit;

    public Long expiryDate;

    public String imagePath;
}