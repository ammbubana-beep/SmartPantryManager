package com.aifani.smartpantrymanager.ui;

import android.content.res.ColorStateList;
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
import java.util.concurrent.TimeUnit;

/** Binds the list of PantryItem rows from the database to the RecyclerView. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    /** Lets the screen react when a row is tapped or long-pressed. */
    public interface OnItemActionListener {
        void onItemClick(PantryItem item);
        void onItemLongClick(PantryItem item);
    }

    /** Items expiring within this many days count as "expiring soon". */
    private static final int SOON_DAYS = 3;
    private static final int COLOR_WARNING = 0xFFD32F2F;

    private List<PantryItem> items = new ArrayList<>();
    private final OnItemActionListener listener;
    private boolean highlightExpiring = true;
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

    /** Turns the expiring-soon highlight on or off (from the Settings screen). */
    public void setHighlightExpiring(boolean enabled) {
        this.highlightExpiring = enabled;
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
            String date = dateFormat.format(new Date(item.expiryDate));
            long millisLeft = item.expiryDate - System.currentTimeMillis();
            long daysLeft = TimeUnit.MILLISECONDS.toDays(millisLeft);

            if (highlightExpiring && millisLeft < 0) {
                holder.textExpiry.setText("Expired: " + date);
                holder.textExpiry.setTextColor(COLOR_WARNING);
            } else if (highlightExpiring && daysLeft <= SOON_DAYS) {
                holder.textExpiry.setText("Expires soon: " + date);
                holder.textExpiry.setTextColor(COLOR_WARNING);
            } else {
                holder.textExpiry.setText("Expires: " + date);
                holder.textExpiry.setTextColor(holder.defaultExpiryColors);
            }
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
        final ColorStateList defaultExpiryColors;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
            defaultExpiryColors = textExpiry.getTextColors();
        }
    }
}