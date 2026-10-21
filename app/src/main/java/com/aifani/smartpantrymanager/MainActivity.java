package com.aifani.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aifani.smartpantrymanager.data.AppDatabase;
import com.aifani.smartpantrymanager.data.DatabaseSeeder;
import com.aifani.smartpantrymanager.data.PantryItem;
import com.aifani.smartpantrymanager.ui.PantryAdapter;
import com.aifani.smartpantrymanager.util.ImageHelper;
import com.aifani.smartpantrymanager.util.NavigationHelper;
import com.aifani.smartpantrymanager.util.SettingsStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemActionListener {

    private static final int SOON_DAYS = 3;

    private PantryAdapter adapter;
    private TextView textEmpty;
    private RecyclerView recyclerPantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
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
        BottomNavigationView nav = findViewById(R.id.bottomNav);

        adapter = new PantryAdapter(this);
        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));
        recyclerPantry.setAdapter(adapter);

        int bottomSpace = (int) (88 * getResources().getDisplayMetrics().density);
        recyclerPantry.setClipToPadding(false);
        recyclerPantry.setPadding(recyclerPantry.getPaddingLeft(),
                recyclerPantry.getPaddingTop(),
                recyclerPantry.getPaddingRight(),
                bottomSpace);

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
        NavigationHelper.setup(this, nav, R.id.nav_pantry);

        bindPulseCard(R.id.cardPulseTotal, AlertsActivity.TAB_ALL);
        bindPulseCard(R.id.cardPulseSoon, AlertsActivity.TAB_SOON);
        bindPulseCard(R.id.cardPulseExpired, AlertsActivity.TAB_EXPIRED);

        AppDatabase.databaseExecutor.execute(() -> {
            DatabaseSeeder.seedIfEmpty(getApplicationContext());
            loadPantry();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setHighlightExpiring(SettingsStore.isHighlightExpiring(this));
        AppDatabase.databaseExecutor.execute(this::loadPantry);
    }

    private void loadPantry() {
        List<PantryItem> items = AppDatabase.getInstance(getApplicationContext())
                .pantryDao().getAll();

        int soon = 0;
        int expired = 0;
        long now = System.currentTimeMillis();
        for (PantryItem item : items) {
            if (item.expiryDate == null) continue;
            long millisLeft = item.expiryDate - now;
            long daysLeft = TimeUnit.MILLISECONDS.toDays(millisLeft);
            if (millisLeft < 0) {
                expired++;
            } else if (daysLeft <= SOON_DAYS) {
                soon++;
            }
        }
        final int soonCount = soon;
        final int expiredCount = expired;

        runOnUiThread(() -> {
            adapter.setItems(items);
            boolean empty = items.isEmpty();
            textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            recyclerPantry.setVisibility(empty ? View.GONE : View.VISIBLE);

            setCount(R.id.tvPulseTotal, items.size());
            setCount(R.id.tvPulseSoon, soonCount);
            setCount(R.id.tvPulseExpired, expiredCount);
        });
    }

    private void setCount(int viewId, int value) {
        TextView view = findViewById(viewId);
        if (view != null) view.setText(String.valueOf(value));
    }

    private void bindPulseCard(int cardId, int tab) {
        View card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> openAlerts(tab));
        }
    }

    private void openAlerts(int tab) {
        Intent intent = new Intent(this, AlertsActivity.class);
        intent.putExtra(AlertsActivity.EXTRA_TAB, tab);
        startActivity(intent);
    }

    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.id);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient?")
                .setMessage("Remove " + item.name + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) ->
                        AppDatabase.databaseExecutor.execute(() -> {
                            AppDatabase.getInstance(getApplicationContext())
                                    .pantryDao().delete(item);
                            ImageHelper.delete(item.imagePath);
                            loadPantry();
                            runOnUiThread(() -> Toast.makeText(this,
                                    "Ingredient deleted", Toast.LENGTH_SHORT).show());
                        }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}