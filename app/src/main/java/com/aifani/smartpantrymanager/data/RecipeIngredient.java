package com.aifani.smartpantrymanager.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One ingredient line of a recipe, e.g. "200 g rice".
 * Linked to its recipe by recipeId; deleting a recipe deletes its lines.
 */
@Entity(
        tableName = "recipe_ingredients",
        foreignKeys = @ForeignKey(
                entity = Recipe.class,
                parentColumns = "id",
                childColumns = "recipeId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("recipeId")}
)
public class RecipeIngredient {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int recipeId;

    /** Name shown on the recipe screen, e.g. "Tomatoes". */
    public String name;

    /** Cleaned-up name used for matching, e.g. "tomato". */
    public String normalizedName;

    /** Amount the recipe needs, stored in the base unit (see below). */
    public double quantity;

    /** Base unit: "g", "ml" or "pcs". */
    public String unit;
}