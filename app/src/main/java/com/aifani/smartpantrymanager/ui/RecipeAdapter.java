package com.aifani.smartpantrymanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aifani.smartpantrymanager.R;
import com.aifani.smartpantrymanager.data.Recipe;
import com.aifani.smartpantrymanager.data.RecipeIngredient;
import com.aifani.smartpantrymanager.data.RecipeMatch;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private static final int COLOR_READY = 0xFF2E7D32;
    private static final int COLOR_MISSING = 0xFFE65100;

    private List<RecipeMatch> matches = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setMatches(List<RecipeMatch> newMatches) {
        this.matches = newMatches;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeMatch match = matches.get(position);
        Recipe recipe = match.recipe;

        holder.textRecipeName.setText(recipe.name);

        if (recipe.cookingTimeMinutes > 0) {
            holder.textRecipeTime.setText("Cooking time: " + recipe.cookingTimeMinutes + " min");
        } else {
            holder.textRecipeTime.setText("Cooking time not set");
        }

        if (match.isReady()) {
            holder.textRecipeStatus.setText("Ready to cook");
            holder.textRecipeStatus.setTextColor(COLOR_READY);
        } else {
            StringBuilder names = new StringBuilder();
            for (RecipeIngredient item : match.missing) {
                if (names.length() > 0) names.append(", ");
                names.append(item.name);
            }
            holder.textRecipeStatus.setText("Missing: " + names);
            holder.textRecipeStatus.setTextColor(COLOR_MISSING);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textRecipeName;
        final TextView textRecipeTime;
        final TextView textRecipeStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textRecipeName = itemView.findViewById(R.id.textRecipeName);
            textRecipeTime = itemView.findViewById(R.id.textRecipeTime);
            textRecipeStatus = itemView.findViewById(R.id.textRecipeStatus);
        }
    }
}