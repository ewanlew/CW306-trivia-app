package com.ewan.triviaapp.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.adapters.TriviaHistoryAdapter
import com.ewan.triviaapp.models.GameHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TriviaHistoryAdapter
    private lateinit var txtGamesPlayed: TextView
    private lateinit var txtWinRate: TextView
    private val gameHistoryList = mutableListOf<GameHistory>()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Get the username from the intent
        val username = intent.getStringExtra("username") ?: ""

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.gameRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TriviaHistoryAdapter(gameHistoryList)
        recyclerView.adapter = adapter

        // Initialize TextViews for stats
        txtGamesPlayed = findViewById(R.id.txtGamesPlayedVal)
        txtWinRate = findViewById(R.id.txtWinPercentageVal)

        // Load game history and update stats
        loadGameHistory(username)
        updateStats(username)
    }

    private fun loadGameHistory(username: String) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val gameHistoryJson = sharedPref.getString("$username:gameHistory", "[]")

        if (gameHistoryJson.isNullOrEmpty()) {
            Log.e("HistoryActivity", "No game history found for user: $username")
            return
        }

        try {
            // Parse the JSON into a list of GameHistory objects
            val type = object : TypeToken<List<GameHistory>>() {}.type
            val historyList: List<GameHistory> = gson.fromJson(gameHistoryJson, type)

            // Add the parsed data to the adapter
            gameHistoryList.clear()
            gameHistoryList.addAll(historyList)
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("HistoryActivity", "Error parsing game history JSON: ${e.message}")
        }
    }

    private fun updateStats(username: String) {
        // Total games played
        val totalGames = gameHistoryList.size
        txtGamesPlayed.text = totalGames.toString()

        // Calculate win rate (perfect games count as wins)
        val wins = gameHistoryList.count { it.score.split("/")[0] == it.score.split("/")[1] }
        val winRate = if (totalGames > 0) (wins.toFloat() / totalGames * 100).toInt() else 0
        txtWinRate.text = "$winRate%"
    }
}
