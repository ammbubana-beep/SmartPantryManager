package com.aifani.smartpantrymanager;

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
import com.aifani.smartpantrymanager.data.RecipeMatch;
import com.aifani.smartpantrymanager.ui.RecipeAdapter;
import com.aifani.smartpantrymanager.util.NavigationHelper;
import com.aifani.smartpantrymanager.util.RecipeMatcher;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private RecipeAdapter adapter;
    private RecyclerView recyclerRecipes;
    private TextView textNoRecipes;
    private TextView textSubtitle;
    private TabLayout tabLayout;

    private List<RecipeMatch> readyList = new ArrayList<>();
    private List<RecipeMatch> missingList = new ArrayList<>();

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
        tabLayout = findViewById(R.id.tabRecipes);

        adapter = new RecipeAdapter(this);
        recyclerRecipes.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecipes.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Ready to cook"));
        tabLayout.addTab(tabLayout.newTab().setText("Missing items"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showCurrentTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        NavigationHelper.setup(this, nav, R.id.nav_recipes);
    }


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

            // The strict-matching rule runs here: nothing missing allowed.
            List<RecipeMatch> ready = new ArrayList<>();
            for (Recipe recipe : RecipeMatcher.findMakeable(pantry, recipes, ingredients)) {
                ready.add(new RecipeMatch(recipe, new ArrayList<>()));
            }
            Collections.sort(ready, (a, b) ->
                    Integer.compare(a.recipe.cookingTimeMinutes, b.recipe.cookingTimeMinutes));

            List<RecipeMatch> missing = RecipeMatcher.findAlmostThere(
                    pantry, recipes, ingredients, RecipeMatcher.ALMOST_THERE_MAX_MISSING);

            runOnUiThread(() -> {
                readyList = ready;
                missingList = missing;
                showCurrentTab();
            });
        });
    }

    private void showCurrentTab() {
        boolean readyTab = tabLayout.getSelectedTabPosition() <= 0;
        List<RecipeMatch> list = readyTab ? readyList : missingList;

        adapter.setMatches(list);
        boolean none = list.isEmpty();
        textNoRecipes.setText(readyTab
                ? "No recipes match your pantry yet - add more ingredients"
                : "No recipes are close to ready yet - add more ingredients");
        textNoRecipes.setVisibility(none ? View.VISIBLE : View.GONE);
        recyclerRecipes.setVisibility(none ? View.GONE : View.VISIBLE);

        if (none) {
            textSubtitle.setText("");
        } else if (readyTab) {
            textSubtitle.setText(list.size() + " recipe(s) you can make right now");
        } else {
            textSubtitle.setText(list.size() + " recipe(s) missing just a few items");
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
        startActivity(intent);
    }
}