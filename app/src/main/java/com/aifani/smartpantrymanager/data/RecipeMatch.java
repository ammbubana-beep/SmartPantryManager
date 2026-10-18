package com.aifani.smartpantrymanager.data;

import java.util.List;

public class RecipeMatch {

    public final Recipe recipe;

    public final List<RecipeIngredient> missing;

    public RecipeMatch(Recipe recipe, List<RecipeIngredient> missing) {
        this.recipe = recipe;
        this.missing = missing;
    }

    public boolean isReady() {
        return missing.isEmpty();
    }
}