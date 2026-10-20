package com.aifani.smartpantrymanager.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aifani.smartpantrymanager.R;
import com.aifani.smartpantrymanager.data.AppDatabase;
import com.aifani.smartpantrymanager.data.PantryItem;

import java.util.ArrayList;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onItemClick(PantryItem item);
        void onItemLongClick(PantryItem item);
        void loadPantry();
    }

    private final Context context;
    private final List<PantryItem> pantryList;
    private final OnItemActionListener listener;
    private boolean highlightExpiring = false;

    public PantryAdapter(Context context) {
        this.context = context;
        this.pantryList = new ArrayList<>();
        this.listener = context instanceof OnItemActionListener ? (OnItemActionListener) context : null;
    }
    public PantryAdapter(Context context, List<PantryItem> list, OnItemActionListener listener) {
        this.context = context;
        this.pantryList = list != null ? list : new ArrayList<>();
        this.listener = listener;
    }
    public void setHighlightExpiring(boolean highlight) {
        this.highlightExpiring = highlight;
        notifyDataSetChanged();
    }
    public void setItems(List<PantryItem> items) {
        pantryList.clear();
        if (items != null) pantryList.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = pantryList.get(position);

        holder.textName.setText(item.name);
        holder.textQuantity.setText(String.valueOf(item.quantity) + " " + String.valueOf(item.unit));

        if (item.expiryDate != null) {
            holder.textExpiry.setText(String.valueOf(item.expiryDate));
        } else {
            holder.textExpiry.setText("");
        }

        if (highlightExpiring) {
            holder.itemView.setAlpha(0.9f);

        }

        if (item.imagePath != null && !item.imagePath.isEmpty()) {
            holder.imageItem.setVisibility(View.VISIBLE);
            holder.textItemEmoji.setVisibility(View.GONE);
        } else {
            holder.imageItem.setVisibility(View.GONE);
            holder.textItemEmoji.setVisibility(View.VISIBLE);
            holder.textItemEmoji.setText("🥫");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return pantryList.size();
    }

    private void showDeleteDialog(PantryItem item) {
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Delete Item")
                .setMessage("Remove " + item.name + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AppDatabase.databaseExecutor.execute(() -> {
                        AppDatabase.getInstance(context).pantryDao().delete(item);
                        if (context instanceof android.app.Activity) {
                            ((android.app.Activity) context).runOnUiThread(() -> {
                                int pos = pantryList.indexOf(item);
                                if (pos != -1) {
                                    pantryList.remove(pos);
                                    notifyItemRemoved(pos);
                                }
                                if (listener != null) listener.loadPantry();
                                Toast.makeText(context, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textQuantity, textExpiry, textItemEmoji;
        ImageView imageItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
            imageItem = itemView.findViewById(R.id.imageItem);
            textItemEmoji = itemView.findViewById(R.id.textItemEmoji);
        }
    }
}