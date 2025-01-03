package com.ewan.triviaapp.activities

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.ewan.triviaapp.R
import com.ewan.triviaapp.notifications.NotificationHelper
import java.util.Calendar

class PreferencesActivity : AppCompatActivity() {

    private lateinit var notificationSwitch: SwitchCompat
    private lateinit var resetTimeText: TextView
    private lateinit var notifEnabledText: TextView
    private lateinit var tvUsername: TextView
    private lateinit var editTimeButton: ImageButton
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var username: String
    private lateinit var resetPasswordButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        notificationSwitch = findViewById(R.id.switchPushNotifications)
        notifEnabledText = findViewById(R.id.tvNotiStatus)
        resetTimeText = findViewById(R.id.tvQuizResetTime)
        editTimeButton = findViewById(R.id.btnEditTime)
        tvUsername = findViewById(R.id.tvEditUsername)
        resetPasswordButton = findViewById(R.id.btnEditPassword)

        username = intent.getStringExtra("username") ?: return
        tvUsername.text = username

        notificationHelper = NotificationHelper(this)

        loadUserPreferences()
        updateNotificationText()

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            handleNotificationToggle(isChecked)
        }

        editTimeButton.setOnClickListener {
            openTimePicker()
        }

        resetPasswordButton.setOnClickListener {
            openResetPasswordDialog()
        }
    }

    /**
     * Open the reset password dialog
     */
    private fun openResetPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reset_password, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.etNewPassword)

        /**
         * Create the dialog and set the click listener for the positive button
         */
        val dialog = AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setView(dialogView)
            .setPositiveButton("Confirm") { _, _ ->
                val newPassword = passwordInput.text.toString().trim()
                if (newPassword.isNotEmpty()) {
                    saveNewPassword(newPassword)
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Password cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    /**
     * Save the new password
     */
    private fun saveNewPassword(newPassword: String) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("$username:password", newPassword)
        editor.apply()
    }

    /**
     * Load the user preferences
     */
    private fun loadUserPreferences() {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val isPushNotificationsEnabled = sharedPref.getBoolean("$username:pushNotificationsEnabled", false)
        val resetTime = sharedPref.getString("$username:triviaResetTime", "12:00 PM") ?: "12:00 PM"

        notificationSwitch.isChecked = isPushNotificationsEnabled
        resetTimeText.text = resetTime
    }

    /**
     * Handle the notification toggle
     */
    private fun handleNotificationToggle(isEnabled: Boolean) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("$username:pushNotificationsEnabled", isEnabled)
        editor.apply()

        if (isEnabled) {
            scheduleNotification()
            Toast.makeText(this, "Push notifications enabled. Please ensure the app has permission.", Toast.LENGTH_SHORT).show()
            notifEnabledText.text = getString(R.string.enabledText)
        } else {
            cancelNotifications()
            Toast.makeText(this, "Push notifications disabled.", Toast.LENGTH_SHORT).show()
            notifEnabledText.text = getString(R.string.disabledText)
        }
    }

    /**
     * Open the time picker
     */
    private fun openTimePicker() {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val currentResetTime = sharedPref.getString("$username:triviaResetTime", "12:00 PM") ?: "12:00 PM"
        val (hour, minute) = parseTimeToHourMinute(currentResetTime)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val formattedTime = formatTime(selectedHour, selectedMinute)
                resetTimeText.text = formattedTime
                saveResetTime(formattedTime)
                scheduleNotification()
            },
            hour,
            minute,
            false // Use 12-hour format
        )
        timePickerDialog.show()
    }

    /**
     * Save the reset time
     */
    private fun saveResetTime(time: String) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("$username:triviaResetTime", time)
        editor.apply()
    }

    /**
     * Schedule the notification
     */
    private fun scheduleNotification() {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val resetTime = sharedPref.getString("$username:triviaResetTime", "12:00 PM") ?: "12:00 PM"

        val (hour, minute) = parseTimeToHourMinute(resetTime)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                // If the time is already past, schedule for the next day
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val notificationTitle = "Trivia Reminder"
        val notificationBody = "It's time for your trivia challenge!"

        notificationHelper.scheduleNotification(this, notificationTitle, notificationBody, calendar.timeInMillis)
    }

    /**
     * Cancel the notifications
     */
    private fun cancelNotifications() {
        notificationHelper.cancelAllNotifications()
    }

    /**
     * Update the notification text
     */
    private fun updateNotificationText() {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val isPushNotificationsEnabled = sharedPref.getBoolean("$username:pushNotificationsEnabled", false)

        notifEnabledText.text = if (isPushNotificationsEnabled) {
            getString(R.string.enabledText)
        } else {
            getString(R.string.disabledText)
        }
    }

    /**
     * Parse the time to hour and minute
     */
    private fun parseTimeToHourMinute(time: String): Pair<Int, Int> {
        val isPM = time.contains("PM")
        val parts = time.replace("AM", "").replace("PM", "").trim().split(":")
        var hour = parts[0].toIntOrNull() ?: 0
        val minute = parts[1].toIntOrNull() ?: 0

        if (isPM && hour < 12) hour += 12
        if (!isPM && hour == 12) hour = 0

        return Pair(hour, minute)
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val isPM = hour >= 12
        val formattedHour = if (hour == 0 || hour == 12) 12 else hour % 12
        val formattedMinute = String.format("%02d", minute)
        val amPm = if (isPM) "PM" else "AM"
        return "$formattedHour:$formattedMinute $amPm"
    }
}
