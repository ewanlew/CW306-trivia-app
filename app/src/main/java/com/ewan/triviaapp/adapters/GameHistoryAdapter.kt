package com.ewan.triviaapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.GameHistory

class GameHistoryAdapter(private val gameHistoryList: List<GameHistory>) :
    RecyclerView.Adapter<GameHistoryAdapter.GameHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return GameHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameHistoryViewHolder, position: Int) {
        val gameHistory = gameHistoryList[position]
        holder.bind(gameHistory)
    }

    override fun getItemCount(): Int {
        return gameHistoryList.size
    }

    class GameHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGrade: TextView = itemView.findViewById(R.id.tvGrade)
        private val tvResult: TextView = itemView.findViewById(R.id.tvResult)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)

        fun bind(gameHistory: GameHistory) {
            tvGrade.text = gameHistory.grade
            tvResult.text = gameHistory.score
            tvCategory.text = gameHistory.category
            tvDifficulty.text = gameHistory.difficulty

            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault())
            val formattedDate = dateFormat.format(gameHistory.dateCompleted)
            tvDateTime.text = formattedDate
        }
    }
}
