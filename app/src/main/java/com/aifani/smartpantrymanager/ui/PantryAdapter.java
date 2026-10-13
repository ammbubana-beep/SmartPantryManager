package com.aifani.smartpantrymanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aifani.smartpantrymanager.R;
import com.aifani.smartpantrymanager.data.PantryItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Binds the list of PantryItem rows from the database to the RecyclerView. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    /** Lets the screen react when a row is tapped or long-pressed. */
    public interface OnItemActionListener {
        void onItemClick(PantryItem item);
        void onItemLongClick(PantryItem item);
    }

    private List<PantryItem> items = new ArrayList<>();
    private final OnItemActionListener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public PantryAdapter(OnItemActionListener listener) {
        this.listener = listener;
    }

    /** Replaces the data and refreshes the list. */
    public void setItems(List<PantryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.textName.setText(item.name);
        holder.textQuantity.setText(formatQuantity(item.quantity) + " " + item.unit);

        if (item.expiryDate != null) {
            holder.textExpiry.setText("Expires: " + dateFormat.format(new Date(item.expiryDate)));
            holder.textExpiry.setVisibility(View.VISIBLE);
        } else {
            holder.textExpiry.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Shows 500 instead of 500.0, but keeps 0.5 as 0.5. */
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    /** Holds references to the views of one row so they are not looked up again. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textName;
        final TextView textQuantity;
        final TextView textExpiry;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
        }
    }
}