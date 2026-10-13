package com.aifani.smartpantrymanager.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PantryDao {

    @Insert
    long insert(PantryItem item);                 // Create

    @Query("SELECT * FROM pantry_items ORDER BY name COLLATE NOCASE")
    List<PantryItem> getAll();                    // Read (list)

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    PantryItem getById(int id);                   // Read (one, for editing)

    @Update
    void update(PantryItem item);                 // Update

    @Delete
    void delete(PantryItem item);                 // Delete
}