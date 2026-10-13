package com.aifani.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aifani.smartpantrymanager.data.AppDatabase;
import com.aifani.smartpantrymanager.data.PantryItem;
import com.aifani.smartpantrymanager.util.TextNormalizer;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Add/Edit Ingredient screen. If an item id is passed in, it edits; otherwise it adds. */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Key for the Intent extra that carries the id of the item being edited. */
    public static final String EXTRA_ITEM_ID = "item_id";

    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputEditText editName;
    private TextInputEditText editQuantity;
    private TextInputEditText editExpiry;
    private Spinner spinnerUnit;
    private Button buttonDelete;

    private PantryItem existingItem;   // null when adding a new item
    private Long expiryMillis;         // null when no expiry date is set

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addEditRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        TextView textFormTitle = findViewById(R.id.textFormTitle);
        layoutName = findViewById(R.id.layoutName);
        layoutQuantity = findViewById(R.id.layoutQuantity);
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonClearExpiry = findViewById(R.id.buttonClearExpiry);
        buttonDelete = findViewById(R.id.buttonDelete);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        editExpiry.setOnClickListener(v -> showDatePicker());
        buttonClearExpiry.setOnClickListener(v -> {
            expiryMillis = null;
            editExpiry.setText("");
        });
        buttonSave.setOnClickListener(v -> saveItem());
        buttonDelete.setOnClickListener(v -> confirmDelete());

        // Read the Intent: did the list screen send us an item id to edit?
        int itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            textFormTitle.setText("Edit ingredient");
            buttonDelete.setVisibility(View.VISIBLE);
            loadItem(itemId);
        }
    }

    /** Loads an existing item from the database and fills the form. */
    private void loadItem(int itemId) {
        AppDatabase.databaseExecutor.execute(() -> {
            PantryItem item = AppDatabase.getInstance(getApplicationContext())
                    .pantryDao().getById(itemId);
            runOnUiThread(() -> {
                if (item == null) {
                    finish();
                    return;
                }
                existingItem = item;
                editName.setText(item.name);
                editQuantity.setText(formatQuantity(item.quantity));
                spinnerUnit.setSelection(indexOfUnit(item.unit));
                expiryMillis = item.expiryDate;
                editExpiry.setText(item.expiryDate == null
                        ? "" : dateFormat.format(item.expiryDate));
            });
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (expiryMillis != null) cal.setTimeInMillis(expiryMillis);

        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day, 12, 0, 0);
            expiryMillis = picked.getTimeInMillis();
            editExpiry.setText(dateFormat.format(picked.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Validates the form; if everything is valid, saves (insert or update). */
    private void saveItem() {
        String name = textOf(editName);
        String quantityText = textOf(editQuantity);
        boolean valid = true;

        // Validate the name
        if (name.isEmpty()) {
            layoutName.setError("Please enter an ingredient name");
            valid = false;
        } else if (name.length() < 2) {
            layoutName.setError("Name must be at least 2 characters");
            valid = false;
        } else {
            layoutName.setError(null);
        }

        // Validate the quantity
        double quantity = 0;
        if (quantityText.isEmpty()) {
            layoutQuantity.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    layoutQuantity.setError("Quantity must be greater than 0");
                    valid = false;
                } else if (quantity > 100000) {
                    layoutQuantity.setError("That quantity is too large");
                    valid = false;
                } else {
                    layoutQuantity.setError(null);
                }
            } catch (NumberFormatException e) {
                layoutQuantity.setError("Enter a valid number");
                valid = false;
            }
        }

        if (!valid) return;

        PantryItem item = (existingItem != null) ? existingItem : new PantryItem();
        item.name = name;
        item.normalizedName = TextNormalizer.normalizeName(name);
        item.quantity = quantity;
        item.unit = (String) spinnerUnit.getSelectedItem();
        item.expiryDate = expiryMillis;

        final boolean isNew = (existingItem == null);
        AppDatabase.databaseExecutor.execute(() -> {
            if (isNew) {
                AppDatabase.getInstance(getApplicationContext()).pantryDao().insert(item);
            } else {
                AppDatabase.getInstance(getApplicationContext()).pantryDao().update(item);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, isNew ? "Ingredient added" : "Ingredient updated",
                        Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void confirmDelete() {
        if (existingItem == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient?")
                .setMessage("Remove " + existingItem.name + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) ->
                        AppDatabase.databaseExecutor.execute(() -> {
                            AppDatabase.getInstance(getApplicationContext())
                                    .pantryDao().delete(existingItem);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Ingredient deleted",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private int indexOfUnit(String unit) {
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equalsIgnoreCase(unit)) return i;
        }
        return 0;
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) return String.valueOf((long) quantity);
        return String.valueOf(quantity);
    }
}