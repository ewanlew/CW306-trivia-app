package com.ewan.triviaapp.adapters

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.GameHistory

class TriviaHistoryAdapter(private val triviaHistoryList: List<GameHistory>) :
    RecyclerView.Adapter<TriviaHistoryAdapter.TriviaHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TriviaHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return TriviaHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: TriviaHistoryViewHolder, position: Int) {
        val gameHistory = triviaHistoryList[position]
        holder.tvGrade.text = gameHistory.grade
        holder.tvResult.text = gameHistory.score
        holder.tvCategory.text = gameHistory.category
        holder.tvDifficulty.text = gameHistory.difficulty
        holder.tvDateTime.text = gameHistory.dateCompleted.toString()

        // Set grade colors dynamically
        when (gameHistory.grade) {
            "S" -> holder.tvGrade.setTextColor(Color.parseColor("#006400"))
            "A" -> holder.tvGrade.setTextColor(Color.GREEN)
            "B" -> holder.tvGrade.setTextColor(Color.YELLOW)
            "C" -> holder.tvGrade.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.orange)
            )
            "D" -> holder.tvGrade.setTextColor(Color.RED)
            "F" -> holder.tvGrade.setTextColor(Color.BLACK)
        }


        holder.bind(gameHistory)
    }

    override fun getItemCount(): Int {
        return triviaHistoryList.size
    }

    class TriviaHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGrade: TextView = itemView.findViewById(R.id.tvGrade)
        val tvResult: TextView = itemView.findViewById(R.id.tvResult)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)

        fun bind(gameHistory: GameHistory) {
            tvGrade.text = gameHistory.grade
            tvResult.text = gameHistory.score
            tvCategory.text = gameHistory.category
            tvDifficulty.text = gameHistory.difficulty.capitalize()

            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault())
            val formattedDate = dateFormat.format(gameHistory.dateCompleted)
            tvDateTime.text = formattedDate
        }
    }
}
