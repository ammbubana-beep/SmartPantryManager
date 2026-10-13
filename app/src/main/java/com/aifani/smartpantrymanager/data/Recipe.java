package com.aifani.smartpantrymanager.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** One recipe. Its ingredients live in the RecipeIngredient table. */
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    /** Preparation steps, one step per line. */
    public String steps;
}