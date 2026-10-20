package com.aifani.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aifani.smartpantrymanager.data.AppDatabase;
import com.aifani.smartpantrymanager.data.PantryItem;
import com.aifani.smartpantrymanager.ui.PantryAdapter;
import com.aifani.smartpantrymanager.util.SettingsStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemActionListener {

    private RecyclerView recyclerPantry;
    private TextView textEmpty;
    private PantryAdapter adapter;
    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerPantry = findViewById(R.id.recyclerPantry);
        textEmpty = findViewById(R.id.textEmpty);
        fabAdd = findViewById(R.id.fabAdd);
        bottomNav = findViewById(R.id.bottomNav);

        adapter = new PantryAdapter(this);
        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));
        recyclerPantry.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_recipes) {
                startActivity(new Intent(MainActivity.this, SuggestedRecipesActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            } else if (id == R.id.nav_pantry) {
                return true;
            }
            return false;
        });

        AppDatabase.databaseExecutor.execute(this::loadPantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setHighlightExpiring(SettingsStore.isHighlightExpiring(this));
        AppDatabase.databaseExecutor.execute(this::loadPantry);
    }

    @Override
    public void loadPantry() {
        List<PantryItem> items = AppDatabase.getInstance(getApplicationContext())
                .pantryDao().getAll();
        runOnUiThread(() -> {
            adapter.setItems(items);
            boolean empty = items.isEmpty();
            textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            recyclerPantry.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onItemClick(PantryItem item) {

        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra("itemId", item.id);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(PantryItem item) {

    }
}