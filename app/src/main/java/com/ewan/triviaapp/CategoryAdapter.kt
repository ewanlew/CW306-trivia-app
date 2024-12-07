package com.ewan.triviaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class CategoryAdapter(
    private val categories: List<TriviaCategory>,
    private val onCategorySelected: (TriviaCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition: Int = -1 // Track the selected position

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val radioButton: RadioButton = view.findViewById(R.id.rbCategory)
        val emojiText: TextView = view.findViewById(R.id.tvEmoji)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.radioButton.text = category.name
        holder.emojiText.text = category.emoji
        holder.radioButton.isChecked = position == selectedPosition

        holder.radioButton.setOnClickListener {
            if (selectedPosition != position) {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition) // Update previous selection
                notifyItemChanged(selectedPosition) // Update current selection
                onCategorySelected(category) // Notify selection change
            }
        }
    }

    override fun getItemCount(): Int = categories.size
}
