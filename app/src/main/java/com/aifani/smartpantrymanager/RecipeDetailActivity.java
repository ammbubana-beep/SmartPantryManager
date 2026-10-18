package com.aifani.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aifani.smartpantrymanager.data.AppDatabase;
import com.aifani.smartpantrymanager.data.PantryItem;
import com.aifani.smartpantrymanager.data.Recipe;
import com.aifani.smartpantrymanager.data.RecipeIngredient;
import com.aifani.smartpantrymanager.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Recipe Detail screen: cooking time, ingredient status and method for one recipe. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private static final int COLOR_READY = 0xFF2E7D32;
    private static final int COLOR_MISSING = 0xFFE65100;

    private int recipeId;
    private TextView textName;
    private TextView textTime;
    private TextView textStatus;
    private TextView textIngredients;
    private TextView textSteps;
    private Button buttonAddMissing;
    private List<RecipeIngredient> currentMissing = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        textName = findViewById(R.id.textDetailName);
        textTime = findViewById(R.id.textDetailTime);
        textStatus = findViewById(R.id.textDetailStatus);
        textIngredients = findViewById(R.id.textDetailIngredients);
        textSteps = findViewById(R.id.textDetailSteps);
        buttonAddMissing = findViewById(R.id.buttonAddMissing);

        recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        if (recipeId == -1) {
            finish();
            return;
        }

        buttonAddMissing.setOnClickListener(v -> confirmAddMissing());
    }

    /** Reload every time the screen is shown so the status matches the pantry. */
    @Override
    protected void onResume() {
        super.onResume();
        if (recipeId != -1) loadRecipe();
    }

    private void loadRecipe() {
        AppDatabase.databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            Recipe recipe = db.recipeDao().getRecipeById(recipeId);
            List<RecipeIngredient> ingredients = db.recipeDao().getIngredientsForRecipe(recipeId);
            List<PantryItem> pantry = db.pantryDao().getAll();

            List<RecipeIngredient> missing = RecipeMatcher.findMissing(pantry, ingredients);

            Map<String, Double> shortfall = new HashMap<>();
            for (RecipeIngredient gap : missing) {
                shortfall.put(gap.normalizedName + "|" + gap.unit, gap.quantity);
            }

            runOnUiThread(() -> {
                if (recipe == null) {
                    finish();
                    return;
                }
                currentMissing = missing;

                textName.setText(recipe.name);
                textSteps.setText(recipe.steps);
                textTime.setText(recipe.cookingTimeMinutes > 0
                        ? "Cooking time: " + recipe.cookingTimeMinutes + " min"
                        : "Cooking time not set");

                if (missing.isEmpty()) {
                    textStatus.setText("Ready to cook - you have everything");
                    textStatus.setTextColor(COLOR_READY);
                    buttonAddMissing.setVisibility(android.view.View.GONE);
                } else {
                    textStatus.setText("Missing " + missing.size() + " item(s)");
                    textStatus.setTextColor(COLOR_MISSING);
                    buttonAddMissing.setVisibility(android.view.View.VISIBLE);
                }

                StringBuilder sb = new StringBuilder();
                for (RecipeIngredient ri : ingredients) {
                    Double gap = shortfall.get(ri.normalizedName + "|" + ri.unit);
                    if (gap == null) {
                        sb.append("\u2713 ")
                                .append(ri.name).append(" - ")
                                .append(formatQuantity(ri.quantity)).append(" ").append(ri.unit)
                                .append("\n");
                    } else {
                        sb.append("\u2717 ")
                                .append(ri.name).append(" - ")
                                .append(formatQuantity(ri.quantity)).append(" ").append(ri.unit)
                                .append(" (need ").append(formatQuantity(gap)).append(" more)")
                                .append("\n");
                    }
                }
                textIngredients.setText(sb.toString().trim());
            });
        });
    }


    private void confirmAddMissing() {
        if (currentMissing.isEmpty()) return;
        new AlertDialog.Builder(this)
                .setTitle("Add missing items?")
                .setMessage("Add " + currentMissing.size()
                        + " missing item(s) to your pantry? Only do this after you have bought them.")
                .setPositiveButton("Add", (dialog, which) -> addMissingToPantry())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addMissingToPantry() {
        final List<RecipeIngredient> toAdd = new ArrayList<>(currentMissing);
        AppDatabase.databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            for (RecipeIngredient gap : toAdd) {
                PantryItem item = new PantryItem();
                item.name = gap.name;
                item.normalizedName = gap.normalizedName;
                item.quantity = gap.quantity;
                item.unit = gap.unit;
                item.expiryDate = null;
                db.pantryDao().insert(item);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Added to your pantry", Toast.LENGTH_SHORT).show();
                loadRecipe();
            });
        });
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) return String.valueOf((long) quantity);
        return String.valueOf(quantity);
    }
}