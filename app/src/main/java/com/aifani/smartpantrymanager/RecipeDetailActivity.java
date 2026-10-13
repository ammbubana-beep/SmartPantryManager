package com.aifani.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aifani.smartpantrymanager.data.AppDatabase;
import com.aifani.smartpantrymanager.data.Recipe;
import com.aifani.smartpantrymanager.data.RecipeIngredient;

import java.util.List;

/** Recipe Detail screen: full ingredient list and method for one recipe. */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Key for the Intent extra that carries the recipe id. */
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        TextView textName = findViewById(R.id.textDetailName);
        TextView textIngredients = findViewById(R.id.textDetailIngredients);
        TextView textSteps = findViewById(R.id.textDetailSteps);

        // Read the recipe id that the previous screen sent in the Intent.
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        if (recipeId == -1) {
            finish();
            return;
        }

        AppDatabase.databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            Recipe recipe = db.recipeDao().getRecipeById(recipeId);
            List<RecipeIngredient> ingredients = db.recipeDao().getIngredientsForRecipe(recipeId);

            runOnUiThread(() -> {
                if (recipe == null) {
                    finish();
                    return;
                }
                textName.setText(recipe.name);
                textSteps.setText(recipe.steps);

                StringBuilder sb = new StringBuilder();
                for (RecipeIngredient ri : ingredients) {
                    sb.append("\u2022 ")
                            .append(ri.name)
                            .append(" - ")
                            .append(formatQuantity(ri.quantity))
                            .append(" ")
                            .append(ri.unit)
                            .append("\n");
                }
                textIngredients.setText(sb.toString().trim());
            });
        });
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) return String.valueOf((long) quantity);
        return String.valueOf(quantity);
    }
}