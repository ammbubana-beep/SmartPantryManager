package com.aifani.smartpantrymanager;

import com.aifani.smartpantrymanager.util.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aifani.smartpantrymanager.data.AppDatabase;
import com.aifani.smartpantrymanager.data.PantryItem;
import com.aifani.smartpantrymanager.data.Recipe;
import com.aifani.smartpantrymanager.data.RecipeIngredient;
import com.aifani.smartpantrymanager.ui.RecipeAdapter;
import com.aifani.smartpantrymanager.util.RecipeMatcher;

import java.util.List;

/** Suggested Recipes screen: lists ONLY recipes the pantry can fully cover. */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private RecipeAdapter adapter;
    private RecyclerView recyclerRecipes;
    private TextView textNoRecipes;
    private TextView textSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.suggestedRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        recyclerRecipes = findViewById(R.id.recyclerRecipes);
        textNoRecipes = findViewById(R.id.textNoRecipes);
        textSubtitle = findViewById(R.id.textSuggestedSubtitle);

        adapter = new RecipeAdapter(this);
        recyclerRecipes.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecipes.setAdapter(adapter);
    }

    /** Re-run the matching every time the screen is shown, so it reflects the pantry. */
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        AppDatabase.databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());

            List<PantryItem> pantry = db.pantryDao().getAll();
            List<Recipe> recipes = db.recipeDao().getAllRecipes();
            List<RecipeIngredient> ingredients = db.recipeDao().getAllIngredients();

            // The strict-matching rule runs here.
            List<Recipe> makeable = RecipeMatcher.findMakeable(pantry, recipes, ingredients);

            runOnUiThread(() -> {
                adapter.setRecipes(makeable);
                boolean none = makeable.isEmpty();
                textNoRecipes.setVisibility(none ? View.VISIBLE : View.GONE);
                recyclerRecipes.setVisibility(none ? View.GONE : View.VISIBLE);
                textSubtitle.setText(none ? "" : makeable.size() + " recipe(s) you can make right now");
            });
        });
    }

    /** Opens the detail screen and passes the recipe id in the Intent. */
    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
        startActivity(intent);
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        NavigationHelper.setup(this, nav, R.id.nav_recipes);
    }

}