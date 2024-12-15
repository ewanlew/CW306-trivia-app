package com.ewan.triviaapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.TriviaCategory


class CategoryAdapter(
    private val categories: List<TriviaCategory>,
    private val onCategorySelected: (TriviaCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition: Int = -1

    /**
     * ViewHolder class for the category items
     */
    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val radioButton: RadioButton = view.findViewById(R.id.rbCategory)
        val emojiText: TextView = view.findViewById(R.id.tvEmoji)
    }

    /**
     * Create the ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    /**
     * Bind the data to the ViewHolder
     */
    override fun onBindViewHolder(holder: CategoryViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val category = categories[position]
        holder.radioButton.text = category.name
        holder.emojiText.text = category.emoji
        holder.radioButton.isChecked = position == selectedPosition

        holder.radioButton.setOnClickListener {
            if (selectedPosition != position) {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onCategorySelected(category)
            }
        }
    }

    override fun getItemCount(): Int = categories.size
}
