package com.aifani.smartpantrymanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aifani.smartpantrymanager.data.AppDatabase;
import com.aifani.smartpantrymanager.data.PantryItem;
import com.aifani.smartpantrymanager.data.Recipe;
import com.aifani.smartpantrymanager.data.RecipeIngredient;
import com.aifani.smartpantrymanager.util.IngredientIcons;
import com.aifani.smartpantrymanager.util.RecipeImages;
import com.aifani.smartpantrymanager.util.RecipeMatcher;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeDetailActivity extends AppCompatActivity {
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private int recipeId;
    private TextView textName;
    private TextView textTime;
    private TextView textDifficulty;
    private TextView textStatus;
    private TextView textIngredientsTitle;
    private TextView textIngredients;
    private TextView textDetailEmoji;
    private TextView textTip;
    private MaterialCardView cardTip;
    private LinearLayout layoutSteps;
    private ImageView imageDetail;
    private Button buttonAddMissing;
    private List<RecipeIngredient> currentMissing = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            return insets;
        });

        textName = findViewById(R.id.textDetailName);
        textTime = findViewById(R.id.textDetailTime);
        textDifficulty = findViewById(R.id.textDetailDifficulty);
        textStatus = findViewById(R.id.textDetailStatus);
        textIngredientsTitle = findViewById(R.id.textIngredientsTitle);
        textIngredients = findViewById(R.id.textDetailIngredients);
        textDetailEmoji = findViewById(R.id.textDetailEmoji);
        textTip = findViewById(R.id.textDetailTip);
        cardTip = findViewById(R.id.cardDetailTip);
        layoutSteps = findViewById(R.id.layoutDetailSteps);
        imageDetail = findViewById(R.id.imageDetail);
        buttonAddMissing = findViewById(R.id.buttonAddMissing);

        recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        if (recipeId == -1) {
            finish();
            return;
        }

        buttonAddMissing.setOnClickListener(v -> confirmAddMissing());
    }

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
                RecipeImages.apply(this, recipe.name, imageDetail, textDetailEmoji);

                int stepCount = buildSteps(recipe.steps);


                textTime.setText(recipe.cookingTimeMinutes > 0
                        ? "\u23F1 " + recipe.cookingTimeMinutes + " min"
                        : "\u23F1 Time not set");
                textDifficulty.setText("\uD83D\uDC68\u200D\uD83C\uDF73 "
                        + difficultyFor(recipe.cookingTimeMinutes, stepCount));


                if (missing.isEmpty()) {
                    textStatus.setText("\u2713 Ready to cook - you have everything");
                    textStatus.setBackgroundResource(R.drawable.bg_pill_ready);
                    textStatus.setTextColor(ContextCompat.getColor(this, R.color.ready_text));
                    buttonAddMissing.setVisibility(View.GONE);
                } else {
                    textStatus.setText("Missing " + missing.size() + " item(s)");
                    textStatus.setBackgroundResource(R.drawable.bg_pill_warn);
                    textStatus.setTextColor(ContextCompat.getColor(this, R.color.stat_coming_up));
                    buttonAddMissing.setVisibility(View.VISIBLE);
                }

                String tip = tipFor(recipe.name);
                if (tip == null) {
                    cardTip.setVisibility(View.GONE);
                } else {
                    textTip.setText(tip);
                    cardTip.setVisibility(View.VISIBLE);
                }

                textIngredientsTitle.setText("Ingredients (" + ingredients.size() + ")");
                StringBuilder sb = new StringBuilder();
                for (RecipeIngredient ri : ingredients) {
                    Double gap = shortfall.get(ri.normalizedName + "|" + ri.unit);
                    sb.append(gap == null ? "\u2713 " : "\u2717 ")
                            .append(IngredientIcons.emojiFor(ri.normalizedName)).append(" ")
                            .append(ri.name).append(" - ")
                            .append(formatQuantity(ri.quantity)).append(" ").append(ri.unit);
                    if (gap != null) {
                        sb.append(" (need ").append(formatQuantity(gap)).append(" more)");
                    }
                    sb.append("\n");
                }
                textIngredients.setText(sb.toString().trim());
            });
        });
    }

    private int buildSteps(String steps) {
        layoutSteps.removeAllViews();
        if (steps == null) return 0;
        int n = 0;
        for (String line : steps.split("\\r?\\n")) {
            String s = line.trim();
            if (s.isEmpty()) continue;
            s = s.replaceFirst("^\\d+[.)]\\s*", "");
            n++;
            View row = getLayoutInflater().inflate(R.layout.item_step, layoutSteps, false);
            ((TextView) row.findViewById(R.id.textStepNumber)).setText(String.valueOf(n));
            ((TextView) row.findViewById(R.id.textStepBody)).setText(s);
            layoutSteps.addView(row);
        }
        return n;
    }

    private String difficultyFor(int minutes, int steps) {
        if (minutes <= 15 && steps <= 4) return "Easy";
        if (minutes <= 30 && steps <= 6) return "Medium";
        return "Hard";
    }

    private String tipFor(String name) {
        if (name == null) return null;
        String n = name.toLowerCase(Locale.ROOT);
        if (n.contains("omelette"))
            return "Cook on a medium heat. High heat makes the eggs rubbery, and the omelette is ready when the top is just set.";
        if (n.contains("toastie"))
            return "Butter the outside of the bread, not the inside, for a golden, crisp crust.";
        if (n.contains("scrambled"))
            return "Take the pan off the heat while the eggs still look slightly wet. They finish cooking on the way to the plate.";
        if (n.contains("french toast"))
            return "Use bread that is a day or two old. It soaks up the egg mix without falling apart.";
        if (n.contains("macaroni"))
            return "Add a splash of the pasta water to the sauce if it gets too thick.";
        return null;
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