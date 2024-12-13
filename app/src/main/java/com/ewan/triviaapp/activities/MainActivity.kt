package com.ewan.triviaapp.activities

import com.ewan.triviaapp.adapters.UserAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.GameHistory
import com.ewan.triviaapp.models.User
import com.ewan.triviaapp.models.UserProfile
import com.google.gson.Gson
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()
    private val DEFAULT_AVATAR_RES_ID = R.drawable.avi_default
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.userList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        userAdapter = UserAdapter(userList) { user ->
            handleUserSelection(user)
        }
        recyclerView.adapter = userAdapter

        loadUsers()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val addUserButton =
            findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
                R.id.btnAddUser
            )
        addUserButton.setOnClickListener {
            showAddUserDialog()
        }
    }

    private fun showAddUserDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_user, null)
        val usernameInput = dialogView.findViewById<EditText>(R.id.etUsername)
        val passwordInput = dialogView.findViewById<EditText>(R.id.etPassword)
        val saveButton = dialogView.findViewById<Button>(R.id.btnSaveUser)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val userProfile = UserProfile(
                    username = username,
                    password = password,
                    avatarResId = DEFAULT_AVATAR_RES_ID,
                    streak = 0,
                    gems = 0,
                    hardcoreUnlocked = false,
                    gameHistory = mutableListOf(),
                    unlockedAvatars = mutableListOf(DEFAULT_AVATAR_RES_ID),
                    pushNotificationsEnabled = false,
                    triviaResetTime = "12:00 PM"
                )
                saveUserProfile(userProfile)
                userList.add(User(username, DEFAULT_AVATAR_RES_ID))
                userAdapter.notifyItemInserted(userList.size - 1)
                dialog.dismiss()
            } else {
                Log.e("Error", "Fields cannot be empty")
            }
        }
        dialog.show()
    }

    private fun saveUserProfile(userProfile: UserProfile) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putString("${userProfile.username}:password", userProfile.password)
        editor.putInt("${userProfile.username}:avatar", userProfile.avatarResId)
        editor.putInt("${userProfile.username}:streak", userProfile.streak)
        editor.putInt("${userProfile.username}:gems", userProfile.gems)
        editor.putBoolean("${userProfile.username}:hardcoreUnlocked", userProfile.hardcoreUnlocked)

        val gameHistoryJson = gson.toJson(userProfile.gameHistory)
        editor.putString("${userProfile.username}:gameHistory", gameHistoryJson)

        val unlockedAvatarsJson = gson.toJson(userProfile.unlockedAvatars)
        editor.putString("${userProfile.username}:unlockedAvatars", unlockedAvatarsJson)

        editor.putBoolean("${userProfile.username}:pushNotificationsEnabled", userProfile.pushNotificationsEnabled)
        editor.putString("${userProfile.username}:triviaResetTime", userProfile.triviaResetTime)

        editor.apply()
        Log.d("SaveUserProfile", "Saved profile for ${userProfile.username}")
    }

    private fun loadUserProfile(username: String): UserProfile? {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val password = sharedPref.getString("$username:password", null) ?: return null
        val avatarResId = sharedPref.getInt("$username:avatar", DEFAULT_AVATAR_RES_ID)
        val streak = sharedPref.getInt("$username:streak", 0)
        val gems = sharedPref.getInt("$username:gems", 0)
        val hardcoreUnlocked = sharedPref.getBoolean("$username:hardcoreUnlocked", false)
        val triviaResetTime = sharedPref.getString("$username:triviaResetTime", "12:00 PM") ?: "12:00 PM"
        val pushNotificationsEnabled = sharedPref.getBoolean("$username:pushNotificationsEnabled", false)

        val gameHistoryJson = sharedPref.getString("$username:gameHistory", null)
        val gameHistory = if (gameHistoryJson != null) {
            gson.fromJson(gameHistoryJson, Array<GameHistory>::class.java).toMutableList()
        } else {
            mutableListOf()
        }

        val unlockedAvatarsJson = sharedPref.getString("$username:unlockedAvatars", null)
        val unlockedAvatars = if (unlockedAvatarsJson != null) {
            gson.fromJson(unlockedAvatarsJson, Array<Int>::class.java).toMutableList()
        } else {
            mutableListOf()
        }

        return UserProfile(
            username = username,
            password = password,
            avatarResId = avatarResId,
            streak = streak,
            gems = gems,
            hardcoreUnlocked = hardcoreUnlocked,
            gameHistory = gameHistory,
            unlockedAvatars = unlockedAvatars,
            pushNotificationsEnabled = pushNotificationsEnabled,
            triviaResetTime = triviaResetTime
        )
    }

    private fun loadUsers() {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        for (entry in sharedPref.all) {
            if (entry.key.contains(":password")) {
                val username = entry.key.split(":")[0]
                val userProfile = loadUserProfile(username)
                if (userProfile != null) {
                    userList.add(User(username, userProfile.avatarResId))
                }
            }
        }
        userAdapter.notifyDataSetChanged()
    }

    private fun handleUserSelection(user: User) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_password_prompt, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.etPasswordPrompt)
        val submitButton = dialogView.findViewById<Button>(R.id.btnSubmitPassword)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        submitButton.setOnClickListener {
            val enteredPassword = passwordInput.text.toString().trim()
            val userProfile = loadUserProfile(user.username)

            if (userProfile != null && enteredPassword == userProfile.password) {
                dialog.dismiss()
                navigateToHome(userProfile)
            } else {
                passwordInput.error = "Incorrect password"
            }
        }

        dialog.show()
    }

    private fun navigateToHome(userProfile: UserProfile) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("username", userProfile.username)
        intent.putExtra("avatarResId", userProfile.avatarResId)
        startActivity(intent)
    }
}
