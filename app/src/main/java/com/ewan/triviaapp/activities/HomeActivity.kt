package com.ewan.triviaapp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ewan.triviaapp.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

class HomeActivity : AppCompatActivity() {

    private lateinit var txtHourVal: TextView
    private lateinit var txtMinVal: TextView
    private lateinit var txtSecVal: TextView
    private lateinit var txtStreak: TextView
    private lateinit var txtGems: TextView

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        username = intent.getStringExtra("username") ?: return

        // Initialize views
        txtHourVal = findViewById(R.id.txtNewQuestionHourVal)
        txtMinVal = findViewById(R.id.txtNewQuestionMinVal)
        txtSecVal = findViewById(R.id.txtNewQuestionSecVal)
        txtStreak = findViewById(R.id.txtStreak)
        txtGems = findViewById(R.id.txtCoins)

        // Retrieve user data from SharedPreferences
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val streak = sharedPref.getInt("$username:currentStreak", 0)
        val gems = sharedPref.getInt("$username:currentGems", 0)

        // Display the streak and gems
        txtStreak.text = getString(R.string.streakShowcase, streak)
        txtGems.text = getString(R.string.gemsShowcase, gems)

        // Setup buttons
        val startButton = findViewById<Button>(R.id.btnStart)
        startButton.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        val historyButton = findViewById<Button>(R.id.btnHistory)
        historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        val preferencesButton = findViewById<Button>(R.id.btnPreferences)
        preferencesButton.setOnClickListener {
            val intent = Intent(this, PreferencesActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        val collectionButton = findViewById<Button>(R.id.btnCollection)
        collectionButton.setOnClickListener {
            val intent = Intent(this, CollectionActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        // Start the countdown timer
        startCountdownTimer(username)
    }

    /**
     * Start the countdown timer
     */
    private fun startCountdownTimer(username: String) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val resetTime = sharedPref.getString("$username:triviaResetTime", "12:00 PM") ?: "12:00 PM"
        val targetTime = parseTimeToMillis(resetTime)

        Log.d("HomeActivity", "Fetched resetTime: $resetTime")
        Log.d("HomeActivity", "Calculated targetTime: $targetTime")

        handler.removeCallbacksAndMessages(null)

        /**
         * Update the countdown every second
         */
        handler.post(object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val dailyMillis = TimeUnit.DAYS.toMillis(1)
                val timeRemaining = if (currentTime % dailyMillis > targetTime) {
                    dailyMillis - (currentTime % dailyMillis - targetTime)
                } else {
                    targetTime - (currentTime % dailyMillis)
                }

                if (timeRemaining > 0) {
                    updateCountdownDisplay(timeRemaining)
                    handler.postDelayed(this, 1000)
                } else {
                    resetCountdown()
                }
            }
        })
    }

    /**
     * Parse the time string to milliseconds
     */
    private fun parseTimeToMillis(time: String): Long {
        val isPM = time.contains("PM", ignoreCase = true)
        val cleanedTime = time.replace("AM", "", ignoreCase = true)
            .replace("PM", "", ignoreCase = true)
            .trim()

        val parts = cleanedTime.split(":").map { it.toIntOrNull() ?: 0 }
        var hour = parts[0]
        val minute = parts.getOrElse(1) { 0 }

        if (isPM && hour < 12) {
            hour += 12
        }
        if (!isPM && hour == 12) {
            hour = 0
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis % TimeUnit.DAYS.toMillis(1)
    }


    @SuppressLint("DefaultLocale")
    /**
     * Update the countdown display
     */
    private fun updateCountdownDisplay(timeRemaining: Long) {
        val hours = TimeUnit.MILLISECONDS.toHours(timeRemaining)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemaining) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60

        txtHourVal.text = String.format("%02d", hours)
        txtMinVal.text = String.format("%02d", minutes)
        txtSecVal.text = String.format("%02d", seconds)
    }

    @SuppressLint("SetTextI18n")
    /**
     * Resets the countdown
     */
    private fun resetCountdown() {
        txtHourVal.text = "00"
        txtMinVal.text = "00"
        txtSecVal.text = "00"
    }

    /**
     * Stop the handler when the activity is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop the handler when activity is destroyed
    }

    /**
     * Update the streak and gems when the activity is resumed
     */
    override fun onResume() {
        super.onResume()
        updateStreakAndGems()

        reloadResetTimeAndRestartTimer()
    }

    /**
     * Reload the reset time and restart the timer
     */
    private fun reloadResetTimeAndRestartTimer() {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val resetTime = sharedPref.getString("$username:triviaResetTime", "12:00 PM") ?: "12:00 PM"

        Log.d("HomeActivity", "Reloaded resetTime: $resetTime")
        startCountdownTimer(username)
    }


    /**
     * Updates the streak and gems when activity is resumed
     */
    private fun updateStreakAndGems() {
        val username = intent.getStringExtra("username") ?: return

        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val streak = sharedPref.getInt("$username:currentStreak", 0)
        val gems = sharedPref.getInt("$username:currentGems", 0)

        txtStreak.text = getString(R.string.streakShowcase, streak)
        txtGems.text = getString(R.string.gemsShowcase, gems)
    }

}
