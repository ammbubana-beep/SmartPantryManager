package com.aifani.smartpantrymanager.data;

import android.content.Context;

import com.aifani.smartpantrymanager.util.TextNormalizer;
import com.aifani.smartpantrymanager.util.UnitConverter;

import java.util.ArrayList;
import java.util.List;

/** Pre-loads the recipe collection the first time the app runs. */
public final class DatabaseSeeder {

    private DatabaseSeeder() { }

    /** Must be called off the main thread. Does nothing if recipes already exist. */
    public static void seedIfEmpty(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        if (db.recipeDao().countRecipes() > 0) return;

        add(db, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Whisk the eggs and scramble them in melted butter.\n3. Serve on the toast.",
                "eggs|2|pcs", "bread|2|pcs", "butter|10|g");

        add(db, "Cheese Omelette",
                "1. Whisk the eggs.\n2. Cook in butter until almost set.\n3. Add grated cheese, fold and serve.",
                "eggs|3|pcs", "cheese|50|g", "butter|10|g");

        add(db, "Tomato Pasta",
                "1. Boil the pasta.\n2. Fry onion and garlic in oil, add chopped tomatoes and simmer.\n3. Toss with the pasta.",
                "pasta|200|g", "tomatoes|3|pcs", "onion|1|pcs", "garlic|2|pcs", "olive oil|2|tbsp");

        add(db, "Egg Fried Rice",
                "1. Fry chopped onion in oil.\n2. Add cooked rice and soy sauce.\n3. Push aside, scramble the eggs, then mix together.",
                "rice|200|g", "eggs|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "cooking oil|1|tbsp");

        add(db, "Cheese Toastie",
                "1. Butter the bread.\n2. Fill with cheese.\n3. Toast in a pan until golden on both sides.",
                "bread|2|pcs", "cheese|60|g", "butter|10|g");

        add(db, "Mashed Potatoes",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash with butter.\n3. Stir in warm milk until smooth.",
                "potatoes|4|pcs", "butter|30|g", "milk|100|ml");

        add(db, "Pancakes",
                "1. Mix flour, sugar, milk and egg into a batter.\n2. Fry ladles of batter in butter.\n3. Flip when bubbles form.",
                "flour|150|g", "milk|250|ml", "eggs|1|pcs", "sugar|1|tbsp", "butter|20|g");

        add(db, "Tuna Pasta",
                "1. Boil the pasta.\n2. Fry chopped onion in oil.\n3. Mix in the drained tuna and the pasta.",
                "pasta|200|g", "tuna|1|pcs", "onion|1|pcs", "olive oil|1|tbsp");

        add(db, "Lentil Soup",
                "1. Fry onion, carrots and garlic.\n2. Add lentils and stock.\n3. Simmer for 25 minutes until soft.",
                "lentils|200|g", "onion|1|pcs", "carrots|2|pcs", "garlic|2|pcs", "vegetable stock|500|ml");

        add(db, "Beans on Toast",
                "1. Warm the baked beans in a pot.\n2. Toast and butter the bread.\n3. Pour the beans over the toast.",
                "baked beans|1|pcs", "bread|2|pcs", "butter|10|g");

        add(db, "Vegetable Stir-Fry",
                "1. Slice the carrots, cabbage and onion.\n2. Stir-fry in hot oil for 5 minutes.\n3. Add soy sauce and serve.",
                "carrots|2|pcs", "cabbage|200|g", "onion|1|pcs", "soy sauce|2|tbsp", "cooking oil|1|tbsp");

        add(db, "Chicken and Rice",
                "1. Brown the chicken and onion.\n2. Add rice and stock.\n3. Cover and simmer for 20 minutes.",
                "chicken breasts|2|pcs", "rice|200|g", "onion|1|pcs", "chicken stock|500|ml");

        add(db, "Garlic Bread",
                "1. Mix butter with crushed garlic.\n2. Spread on the bread.\n3. Bake at 200 C for 10 minutes.",
                "bread|4|pcs", "butter|40|g", "garlic|2|pcs");

        add(db, "Banana Oat Pancakes",
                "1. Mash the bananas.\n2. Mix in eggs and oats.\n3. Fry small pancakes until golden.",
                "bananas|2|pcs", "eggs|2|pcs", "oats|50|g");

        add(db, "Creamy Porridge",
                "1. Heat the milk and oats together.\n2. Stir for 5 minutes until thick.\n3. Top with honey.",
                "oats|80|g", "milk|300|ml", "honey|1|tbsp");

        add(db, "Fried Potato and Onion",
                "1. Slice the potatoes and onion.\n2. Fry in oil on medium heat for 15 minutes.\n3. Turn often until crisp.",
                "potatoes|3|pcs", "onion|1|pcs", "cooking oil|2|tbsp");

        add(db, "Tomato Soup",
                "1. Fry onion and garlic.\n2. Add chopped tomatoes and stock.\n3. Simmer 20 minutes, then blend.",
                "tomatoes|6|pcs", "onion|1|pcs", "garlic|2|pcs", "vegetable stock|500|ml");

        add(db, "Peanut Butter Sandwich",
                "1. Spread peanut butter on one slice of bread.\n2. Top with the second slice.",
                "bread|2|pcs", "peanut butter|2|tbsp");

        add(db, "French Toast",
                "1. Whisk eggs and milk.\n2. Dip the bread slices.\n3. Fry in butter until golden.",
                "bread|4|pcs", "eggs|2|pcs", "milk|100|ml", "butter|20|g");

        add(db, "Macaroni Cheese",
                "1. Boil the macaroni.\n2. Melt butter, add milk and grated cheese to make a sauce.\n3. Mix with the macaroni.",
                "macaroni|250|g", "cheese|150|g", "milk|300|ml", "butter|30|g");
    }

    private static void add(AppDatabase db, String name, String steps, String... lines) {
        Recipe recipe = new Recipe();
        recipe.name = name;
        recipe.steps = steps;
        long recipeId = db.recipeDao().insertRecipe(recipe);

        List<RecipeIngredient> ingredients = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split("\\|");
            RecipeIngredient ri = new RecipeIngredient();
            ri.recipeId = (int) recipeId;
            ri.name = capitalise(parts[0]);
            ri.normalizedName = TextNormalizer.normalizeName(parts[0]);
            ri.quantity = UnitConverter.toBaseQuantity(Double.parseDouble(parts[1]), parts[2]);
            ri.unit = UnitConverter.toBaseUnit(parts[2]);
            ingredients.add(ri);
        }
        db.recipeDao().insertIngredients(ingredients);
    }

    private static String capitalise(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}