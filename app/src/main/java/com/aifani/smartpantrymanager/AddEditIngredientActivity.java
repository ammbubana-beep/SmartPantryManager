package com.aifani.smartpantrymanager;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aifani.smartpantrymanager.data.AppDatabase;
import com.aifani.smartpantrymanager.data.PantryItem;
import com.aifani.smartpantrymanager.util.IngredientIcons;
import com.aifani.smartpantrymanager.util.ImageHelper;
import com.aifani.smartpantrymanager.util.TextNormalizer;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditIngredientActivity extends AppCompatActivity {
    public static final String EXTRA_ITEM_ID = "item_id";

    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputEditText editName;
    private TextInputEditText editQuantity;
    private TextInputEditText editExpiry;
    private Spinner spinnerUnit;
    private Button buttonDelete;
    private Button buttonAddPhoto;
    private Button buttonRemovePhoto;
    private ImageView imagePreview;
    private TextView textPreviewEmoji;

    private PantryItem existingItem;
    private Long expiryMillis;

    private String originalImagePath;
    private String currentImagePath;
    private boolean saved = false;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private final ActivityResultLauncher<String> photoPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                AppDatabase.databaseExecutor.execute(() -> {
                    String path = ImageHelper.saveFromUri(getApplicationContext(), uri);
                    runOnUiThread(() -> {
                        if (path == null) {
                            Toast.makeText(this, "Could not load that picture",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (currentImagePath != null
                                && !currentImagePath.equals(originalImagePath)) {
                            ImageHelper.delete(currentImagePath);
                        }
                        currentImagePath = path;
                        refreshPicture();
                    });
                });
            });

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
        buttonAddPhoto = findViewById(R.id.buttonAddPhoto);
        buttonRemovePhoto = findViewById(R.id.buttonRemovePhoto);
        imagePreview = findViewById(R.id.imagePreview);
        textPreviewEmoji = findViewById(R.id.textPreviewEmoji);

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

        buttonAddPhoto.setOnClickListener(v -> photoPicker.launch("image/*"));
        buttonRemovePhoto.setOnClickListener(v -> {
            if (currentImagePath != null && !currentImagePath.equals(originalImagePath)) {
                ImageHelper.delete(currentImagePath);
            }
            currentImagePath = null;
            refreshPicture();
        });

        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (currentImagePath == null) refreshPicture();
            }
        });
        refreshPicture();

        int itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            textFormTitle.setText("Edit ingredient");
            buttonDelete.setVisibility(View.VISIBLE);
            loadItem(itemId);
        }
    }
    private void refreshPicture() {
        Bitmap photo = ImageHelper.load(currentImagePath);
        if (photo != null) {
            imagePreview.setImageBitmap(photo);
            imagePreview.setVisibility(View.VISIBLE);
            textPreviewEmoji.setVisibility(View.GONE);
            buttonRemovePhoto.setVisibility(View.VISIBLE);
            buttonAddPhoto.setText("Change photo");
        } else {
            imagePreview.setVisibility(View.GONE);
            textPreviewEmoji.setText(IngredientIcons.emojiFor(textOf(editName)));
            textPreviewEmoji.setVisibility(View.VISIBLE);
            buttonRemovePhoto.setVisibility(View.GONE);
            buttonAddPhoto.setText("Add photo");
        }
    }

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
                originalImagePath = item.imagePath;
                currentImagePath = item.imagePath;
                refreshPicture();
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
    private void saveItem() {
        String name = textOf(editName);
        String quantityText = textOf(editQuantity);
        boolean valid = true;

        if (name.isEmpty()) {
            layoutName.setError("Please enter an ingredient name");
            valid = false;
        } else if (name.length() < 2) {
            layoutName.setError("Name must be at least 2 characters");
            valid = false;
        } else {
            layoutName.setError(null);
        }
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
        item.imagePath = currentImagePath;

        final boolean isNew = (existingItem == null);
        final String oldPath = originalImagePath;
        final String newPath = currentImagePath;
        AppDatabase.databaseExecutor.execute(() -> {
            if (isNew) {
                AppDatabase.getInstance(getApplicationContext()).pantryDao().insert(item);
            } else {
                AppDatabase.getInstance(getApplicationContext()).pantryDao().update(item);
            }
            // The old photo is no longer used if it was replaced or removed.
            if (oldPath != null && !oldPath.equals(newPath)) {
                ImageHelper.delete(oldPath);
            }
            runOnUiThread(() -> {
                saved = true;
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
                            ImageHelper.delete(existingItem.imagePath);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Ingredient deleted",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }))
                .setNegativeButton("Cancel", null)
                .show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!saved && currentImagePath != null && !currentImagePath.equals(originalImagePath)) {
            ImageHelper.delete(currentImagePath);
        }
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