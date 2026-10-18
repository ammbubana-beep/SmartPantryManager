package com.aifani.smartpantrymanager.util;

import com.aifani.smartpantrymanager.data.PantryItem;
import com.aifani.smartpantrymanager.data.Recipe;
import com.aifani.smartpantrymanager.data.RecipeIngredient;
import com.aifani.smartpantrymanager.data.RecipeMatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RecipeMatcher {

    private static final double EPSILON = 1e-9;

    public static final int ALMOST_THERE_MAX_MISSING = 2;

    private RecipeMatcher() { }

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

    public static boolean canMake(List<PantryItem> pantry, List<RecipeIngredient> needed) {
        if (needed == null || needed.isEmpty()) return false;
        return countMissing(buildPantryTotals(pantry), needed) == 0;
    }

    public static int countMissing(List<PantryItem> pantry, List<RecipeIngredient> needed) {
        return countMissing(buildPantryTotals(pantry), needed);
    }

    public static List<RecipeIngredient> findMissing(List<PantryItem> pantry,
                                                     List<RecipeIngredient> needed) {
        return missingFor(buildPantryTotals(pantry), needed);
    }


    public static List<RecipeMatch> findAlmostThere(List<PantryItem> pantry,
                                                    List<Recipe> recipes,
                                                    List<RecipeIngredient> allIngredients,
                                                    int maxMissing) {
        Map<String, Double> totals = buildPantryTotals(pantry);
        Map<Integer, List<RecipeIngredient>> byRecipe = groupByRecipe(allIngredients);

        List<RecipeMatch> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            List<RecipeIngredient> needed = byRecipe.get(recipe.id);
            if (needed == null || needed.isEmpty()) continue;

            List<RecipeIngredient> missing = missingFor(totals, needed);
            if (!missing.isEmpty() && missing.size() <= maxMissing) {
                result.add(new RecipeMatch(recipe, missing));
            }
        }
        Collections.sort(result, (a, b) -> Integer.compare(a.missing.size(), b.missing.size()));
        return result;
    }

    private static int countMissing(Map<String, Double> totals, List<RecipeIngredient> needed) {
        return missingFor(totals, needed).size();
    }

    private static List<RecipeIngredient> missingFor(Map<String, Double> totals,
                                                     List<RecipeIngredient> needed) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient ingredient : needed) {
            Double have = totals.get(key(ingredient.normalizedName, ingredient.unit));
            double haveAmount = (have == null) ? 0 : have;

            if (haveAmount + EPSILON < ingredient.quantity) {
                RecipeIngredient gap = new RecipeIngredient();
                gap.recipeId = ingredient.recipeId;
                gap.name = ingredient.name;
                gap.normalizedName = ingredient.normalizedName;
                gap.unit = ingredient.unit;
                gap.quantity = ingredient.quantity - haveAmount;
                missing.add(gap);
            }
        }
        return missing;
    }

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