package com.aifani.smartpantrymanager.util;

import com.aifani.smartpantrymanager.data.PantryItem;
import com.aifani.smartpantrymanager.data.Recipe;
import com.aifani.smartpantrymanager.data.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The strict-matching rule: a recipe qualifies only if EVERY ingredient it needs
 * is in the pantry, in at least the required quantity. No partial matches.
 */
public final class RecipeMatcher {

    /** Tolerance so that 0.1 + 0.2 style rounding never causes a wrong result. */
    private static final double EPSILON = 1e-9;

    private RecipeMatcher() { }

    /** Returns only the recipes the user can make right now. */
    public static List<Recipe> findMakeable(List<PantryItem> pantry,
                                            List<Recipe> recipes,
                                            List<RecipeIngredient> allIngredients) {
        Map<String, Double> totals = buildPantryTotals(pantry);
        Map<Integer, List<RecipeIngredient>> byRecipe = groupByRecipe(allIngredients);

        List<Recipe> makeable = new ArrayList<>();
        for (Recipe recipe : recipes) {
            List<RecipeIngredient> needed = byRecipe.get(recipe.id);
            // A recipe with no ingredients is never suggested.
            if (needed != null && !needed.isEmpty() && countMissing(totals, needed) == 0) {
                makeable.add(recipe);
            }
        }
        return makeable;
    }

    /** True only if every needed ingredient is available in enough quantity. */
    public static boolean canMake(List<PantryItem> pantry, List<RecipeIngredient> needed) {
        if (needed == null || needed.isEmpty()) return false;
        return countMissing(buildPantryTotals(pantry), needed) == 0;
    }

    /** How many ingredients are missing or too small (useful for an "Almost There" list). */
    public static int countMissing(List<PantryItem> pantry, List<RecipeIngredient> needed) {
        return countMissing(buildPantryTotals(pantry), needed);
    }

    private static int countMissing(Map<String, Double> totals, List<RecipeIngredient> needed) {
        int missing = 0;
        for (RecipeIngredient ingredient : needed) {
            Double have = totals.get(key(ingredient.normalizedName, ingredient.unit));
            if (have == null || have + EPSILON < ingredient.quantity) {
                missing++;
            }
        }
        return missing;
    }

    /** Adds up the pantry by "name + base unit", e.g. "egg|pcs" -> 6. */
    private static Map<String, Double> buildPantryTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String name = (item.normalizedName != null && !item.normalizedName.isEmpty())
                    ? item.normalizedName
                    : TextNormalizer.normalizeName(item.name);
            String unit = UnitConverter.toBaseUnit(item.unit);
            double quantity = UnitConverter.toBaseQuantity(item.quantity, item.unit);

            String k = key(name, unit);
            Double existing = totals.get(k);
            totals.put(k, (existing == null ? 0 : existing) + quantity);
        }
        return totals;
    }

    private static Map<Integer, List<RecipeIngredient>> groupByRecipe(List<RecipeIngredient> all) {
        Map<Integer, List<RecipeIngredient>> map = new HashMap<>();
        for (RecipeIngredient ingredient : all) {
            List<RecipeIngredient> list = map.get(ingredient.recipeId);
            if (list == null) {
                list = new ArrayList<>();
                map.put(ingredient.recipeId, list);
            }
            list.add(ingredient);
        }
        return map;
    }

    private static String key(String normalizedName, String baseUnit) {
        return normalizedName + "|" + baseUnit;
    }
}