package com.aifani.smartpantrymanager.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** The single database for the app (SQLite through Room). */
@Database(
        entities = {PantryItem.class, Recipe.class, RecipeIngredient.class},
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PantryDao pantryDao();
    public abstract RecipeDao recipeDao();

    private static volatile AppDatabase instance;

    /** Database work runs here so it never blocks the screen. */
    public static final ExecutorService databaseExecutor =
            Executors.newFixedThreadPool(2);

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "smart_pantry.db").build();
                }
            }
        }
        return instance;
    }
}