package com.aifani.smartpantrymanager;

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
import com.aifani.smartpantrymanager.ui.PantryAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AlertsActivity extends AppCompatActivity {

    public static final String EXTRA_TAB = "alerts_tab";
    public static final int TAB_ALL = 0;
    public static final int TAB_SOON = 1;
    public static final int TAB_EXPIRED = 2;

    private static final int SOON_DAYS = 3;

    private TabLayout tabs;
    private RecyclerView recycler;
    private TextView textEmpty;
    private TextView bannerExpired;
    private TextView bannerSoon;
    private TextView bannerFresh;
    private PantryAdapter adapter;

    private List<PantryItem> allItems = new ArrayList<>();
    private List<PantryItem> soonItems = new ArrayList<>();
    private List<PantryItem> expiredItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.alertsRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tabs = findViewById(R.id.tabAlerts);
        recycler = findViewById(R.id.recyclerAlerts);
        textEmpty = findViewById(R.id.textAlertsEmpty);
        bannerExpired = findViewById(R.id.textAlertExpired);
        bannerSoon = findViewById(R.id.textAlertSoon);
        bannerFresh = findViewById(R.id.textAlertFresh);

        findViewById(R.id.buttonAlertsBack).setOnClickListener(v -> finish());

        adapter = new PantryAdapter(new PantryAdapter.OnItemActionListener() {
            @Override
            public void onItemClick(PantryItem item) { }

            @Override
            public void onItemLongClick(PantryItem item) { }
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        tabs.addTab(tabs.newTab().setText("All"));
        tabs.addTab(tabs.newTab().setText("Expiring soon"));
        tabs.addTab(tabs.newTab().setText("Expired"));

        int start = getIntent().getIntExtra(EXTRA_TAB, TAB_ALL);
        TabLayout.Tab startTab = tabs.getTabAt(start);
        if (startTab != null) startTab.select();

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) { showSelectedTab(); }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        AppDatabase.databaseExecutor.execute(() -> {
            List<PantryItem> all = AppDatabase.getInstance(getApplicationContext())
                    .pantryDao().getAll();

            List<PantryItem> soon = new ArrayList<>();
            List<PantryItem> expired = new ArrayList<>();
            long now = System.currentTimeMillis();

            for (PantryItem item : all) {
                if (item.expiryDate == null) continue;
                long millisLeft = item.expiryDate - now;
                long daysLeft = TimeUnit.MILLISECONDS.toDays(millisLeft);
                if (millisLeft < 0) {
                    expired.add(item);
                } else if (daysLeft <= SOON_DAYS) {
                    soon.add(item);
                }
            }

            Collections.sort(soon, (a, b) -> Long.compare(a.expiryDate, b.expiryDate));
            Collections.sort(expired, (a, b) -> Long.compare(a.expiryDate, b.expiryDate));

            runOnUiThread(() -> {
                allItems = all;
                soonItems = soon;
                expiredItems = expired;
                updateBanners();
                updateTabTitles();
                showSelectedTab();
            });
        });
    }

    private void updateBanners() {
        int expired = expiredItems.size();
        int soon = soonItems.size();

        if (expired > 0) {
            bannerExpired.setText("\u26A0 " + expired + (expired == 1
                    ? " item has expired. Check it now."
                    : " items have expired. Check them now."));
            bannerExpired.setVisibility(View.VISIBLE);
        } else {
            bannerExpired.setVisibility(View.GONE);
        }

        if (soon > 0) {
            bannerSoon.setText("\u23F0 " + soon + (soon == 1
                    ? " item expires within " + SOON_DAYS + " days."
                    : " items expire within " + SOON_DAYS + " days."));
            bannerSoon.setVisibility(View.VISIBLE);
        } else {
            bannerSoon.setVisibility(View.GONE);
        }

        bannerFresh.setVisibility(expired == 0 && soon == 0 ? View.VISIBLE : View.GONE);
    }

    private void updateTabTitles() {
        setTabTitle(TAB_ALL, "All (" + allItems.size() + ")");
        setTabTitle(TAB_SOON, "Expiring soon (" + soonItems.size() + ")");
        setTabTitle(TAB_EXPIRED, "Expired (" + expiredItems.size() + ")");
    }

    private void setTabTitle(int index, String title) {
        TabLayout.Tab tab = tabs.getTabAt(index);
        if (tab != null) tab.setText(title);
    }

    private void showSelectedTab() {
        int position = tabs.getSelectedTabPosition();
        List<PantryItem> list;
        String emptyText;

        if (position == TAB_SOON) {
            list = soonItems;
            emptyText = "\u2705 Nothing is expiring soon.";
        } else if (position == TAB_EXPIRED) {
            list = expiredItems;
            emptyText = "\u2705 Nothing has expired.";
        } else {
            list = allItems;
            emptyText = "Your pantry is empty.\nAdd your first ingredient.";
        }

        adapter.setItems(list);
        textEmpty.setText(emptyText);
        textEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}