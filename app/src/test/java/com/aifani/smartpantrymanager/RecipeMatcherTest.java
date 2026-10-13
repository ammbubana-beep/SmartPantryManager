package com.aifani.smartpantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.aifani.smartpantrymanager.data.PantryItem;
import com.aifani.smartpantrymanager.data.Recipe;
import com.aifani.smartpantrymanager.data.RecipeIngredient;
import com.aifani.smartpantrymanager.util.RecipeMatcher;
import com.aifani.smartpantrymanager.util.TextNormalizer;
import com.aifani.smartpantrymanager.util.UnitConverter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeMatcherTest {

    private PantryItem pantry(String name, double qty, String unit) {
        PantryItem item = new PantryItem();
        item.name = name;
        item.normalizedName = TextNormalizer.normalizeName(name);
        item.quantity = qty;
        item.unit = unit;
        return item;
    }

    private RecipeIngredient need(int recipeId, String name, double qty, String unit) {
        RecipeIngredient ri = new RecipeIngredient();
        ri.recipeId = recipeId;
        ri.name = name;
        ri.normalizedName = TextNormalizer.normalizeName(name);
        ri.quantity = UnitConverter.toBaseQuantity(qty, unit);
        ri.unit = UnitConverter.toBaseUnit(unit);
        return ri;
    }

    // Scrambled eggs on toast: eggs 2 pcs, bread 2 pcs, butter 10 g
    private List<RecipeIngredient> eggsOnToast() {
        return Arrays.asList(
                need(1, "eggs", 2, "pcs"),
                need(1, "bread", 2, "pcs"),
                need(1, "butter", 10, "g"));
    }

    @Test
    public void allIngredientsPresent_recipeIsMakeable() {
        List<PantryItem> pantry = Arrays.asList(
                pantry("Eggs", 6, "pcs"), pantry("Bread", 4, "pcs"), pantry("Butter", 250, "g"));
        assertTrue(RecipeMatcher.canMake(pantry, eggsOnToast()));
    }

    @Test
    public void oneIngredientMissing_recipeIsNotMakeable() {
        // 3 of the 3 needed exist except butter: the "4 out of 5" rule in the brief
        List<PantryItem> pantry = Arrays.asList(
                pantry("Eggs", 6, "pcs"), pantry("Bread", 4, "pcs"));
        assertFalse(RecipeMatcher.canMake(pantry, eggsOnToast()));
        assertEquals(1, RecipeMatcher.countMissing(pantry, eggsOnToast()));
    }

    @Test
    public void quantityTooSmall_recipeIsNotMakeable() {
        List<PantryItem> pantry = Arrays.asList(
                pantry("Eggs", 1, "pcs"), pantry("Bread", 4, "pcs"), pantry("Butter", 250, "g"));
        assertFalse(RecipeMatcher.canMake(pantry, eggsOnToast()));
    }

    @Test
    public void singularAndPluralNamesMatch() {
        // Pantry says "Egg", recipe says "eggs"
        List<PantryItem> pantry = Arrays.asList(
                pantry("Egg", 3, "pcs"), pantry("Breads", 2, "pcs"), pantry("BUTTER ", 20, "g"));
        assertTrue(RecipeMatcher.canMake(pantry, eggsOnToast()));
    }

    @Test
    public void tomatoVsTomatoes() {
        List<RecipeIngredient> needed = Arrays.asList(need(2, "tomatoes", 3, "pcs"));
        assertTrue(RecipeMatcher.canMake(Arrays.asList(pantry("Tomato", 3, "pcs")), needed));
        assertTrue(RecipeMatcher.canMake(Arrays.asList(pantry("Tomatoes", 5, "pcs")), needed));
    }

    @Test
    public void unitDifferencesAreConverted() {
        // Recipe needs 200 g of pasta; pantry has 1 kg
        List<RecipeIngredient> needed = Arrays.asList(need(3, "pasta", 200, "g"));
        assertTrue(RecipeMatcher.canMake(Arrays.asList(pantry("Pasta", 1, "kg")), needed));
        // Recipe needs 1 l of milk; pantry has 500 ml
        List<RecipeIngredient> milk = Arrays.asList(need(4, "milk", 1, "l"));
        assertFalse(RecipeMatcher.canMake(Arrays.asList(pantry("Milk", 500, "ml")), milk));
    }

    @Test
    public void duplicatePantryRowsAreAddedTogether() {
        List<PantryItem> pantry = Arrays.asList(pantry("Eggs", 1, "pcs"), pantry("Eggs", 1, "pcs"),
                pantry("Bread", 2, "pcs"), pantry("Butter", 10, "g"));
        assertTrue(RecipeMatcher.canMake(pantry, eggsOnToast()));
    }

    @Test
    public void emptyPantry_returnsNoRecipes() {
        Recipe recipe = new Recipe();
        recipe.id = 1;
        recipe.name = "Scrambled Eggs on Toast";
        List<Recipe> result = RecipeMatcher.findMakeable(
                new ArrayList<>(), Arrays.asList(recipe), eggsOnToast());
        assertTrue(result.isEmpty());
    }
}