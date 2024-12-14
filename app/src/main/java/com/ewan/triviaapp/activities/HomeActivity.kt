package com.ewan.triviaapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val username = intent.getStringExtra("username") ?: return
        val avatarResId = intent.getIntExtra("avatarResId", R.drawable.avi_default)

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
            intent.putExtra("avatarResId", avatarResId)
            startActivity(intent)
        }

        // Start the countdown timer
        startCountdownTimer(username)
    }

    private fun startCountdownTimer(username: String) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)

        // Retrieve the trivia reset time from SharedPreferences
        val resetTime = sharedPref.getString("$username:triviaResetTime", "12:00") ?: "12:00"
        val targetTime = parseTimeToMillis(resetTime)

        // Schedule the timer update
        handler.post(object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val timeRemaining = targetTime - (currentTime % TimeUnit.DAYS.toMillis(1))

                if (timeRemaining > 0) {
                    updateCountdownDisplay(timeRemaining)
                    handler.postDelayed(this, 1000)
                } else {
                    resetCountdown()
                }
            }
        })
    }

    private fun parseTimeToMillis(time: String): Long {
        val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parts[0])
            set(Calendar.MINUTE, parts[1])
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis % TimeUnit.DAYS.toMillis(1)
    }

    private fun updateCountdownDisplay(timeRemaining: Long) {
        val hours = TimeUnit.MILLISECONDS.toHours(timeRemaining)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemaining) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60

        txtHourVal.text = String.format("%02d", hours)
        txtMinVal.text = String.format("%02d", minutes)
        txtSecVal.text = String.format("%02d", seconds)
    }

    private fun resetCountdown() {
        txtHourVal.text = "00"
        txtMinVal.text = "00"
        txtSecVal.text = "00"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop the handler when activity is destroyed
    }

    override fun onResume() {
        super.onResume()
        // Update streak and gems when the activity is resumed
        updateStreakAndGems()
    }

    private fun updateStreakAndGems() {
        // Retrieve username from Intent
        val username = intent.getStringExtra("username") ?: return

        // Retrieve user data from SharedPreferences
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val streak = sharedPref.getInt("$username:currentStreak", 0)
        val gems = sharedPref.getInt("$username:currentGems", 0)

        // Display the streak and gems
        txtStreak.text = getString(R.string.streakShowcase, streak)
        txtGems.text = getString(R.string.gemsShowcase, gems)
    }

}
