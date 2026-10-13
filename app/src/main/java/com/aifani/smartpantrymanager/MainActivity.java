package com.aifani.smartpantrymanager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aifani.smartpantrymanager.data.AppDatabase;
import com.aifani.smartpantrymanager.data.DatabaseSeeder;
import com.aifani.smartpantrymanager.data.PantryItem;
import com.aifani.smartpantrymanager.ui.PantryAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/** Pantry List screen: shows every ingredient the user currently has. */
public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemActionListener {

    private PantryAdapter adapter;
    private TextView textEmpty;
    private RecyclerView recyclerPantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerPantry = findViewById(R.id.recyclerPantry);
        textEmpty = findViewById(R.id.textEmpty);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        adapter = new PantryAdapter(this);
        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));
        recyclerPantry.setAdapter(adapter);

        fabAdd.setOnClickListener(v ->
                Toast.makeText(this, "Add screen comes in the next step", Toast.LENGTH_SHORT).show());

        // Load the recipe collection on first run, then show the pantry.
        AppDatabase.databaseExecutor.execute(() -> {
            DatabaseSeeder.seedIfEmpty(getApplicationContext());
            loadPantry();
        });
    }

    /** Reloads the list every time the screen comes back to the front. */
    @Override
    protected void onResume() {
        super.onResume();
        AppDatabase.databaseExecutor.execute(this::loadPantry);
    }

    /** Reads all pantry items on a background thread, then updates the screen. */
    private void loadPantry() {
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
        // Opens the edit screen in the next step.
        Toast.makeText(this, "Edit: " + item.name, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClick(PantryItem item) {
        // Delete is added in the next step.
    }
}