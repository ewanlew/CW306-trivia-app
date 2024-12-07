package com.ewan.triviaapp.activities

import com.ewan.triviaapp.adapters.UserAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.User

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()
    private val DEFAULT_AVATAR_RES_ID = R.drawable.avi_default

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

        val addUserButton = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
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
                val newUser = User(username, DEFAULT_AVATAR_RES_ID)
                saveUser(username, password, DEFAULT_AVATAR_RES_ID)
                userList.add(newUser)
                userAdapter.notifyItemInserted(userList.size - 1)
                dialog.dismiss()
            } else {
                Log.e("Error", "Fields cannot be empty")
            }
        }
        dialog.show()
    }

    private fun saveUser(username: String, password: String, avatarResId: Int) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("$username:password", password)
        editor.putInt("$username:avatar", avatarResId)
        editor.apply()
        Log.d("SaveUser", "Saving user: $username, Password: $password, Avatar Res ID: $avatarResId")

    }

    private fun loadUsers() {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        for (entry in sharedPref.all) {
            if (entry.key.contains(":password")) {
                val username = entry.key.split(":")[0]
                val avatarResId = sharedPref.getInt("$username:avatar", DEFAULT_AVATAR_RES_ID)
                Log.d("MainActivity", "Loaded user: $username with Avatar Res ID: $avatarResId")
                userList.add(User(username, avatarResId))
            }
        }
        userAdapter.notifyDataSetChanged()
    }

    private fun clearSharedPreferences() {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        Log.d("SharedPreferences", "Cleared all user data")
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

            val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
            for ((key, value) in sharedPref.all) {
                Log.d("SharedPreferences", "Key: $key, Value: $value")
            }

            val storedPassword = sharedPref.getString("${user.username}:password", "")

            if (enteredPassword == storedPassword) {
                dialog.dismiss()
                navigateToHome(user)
            } else {
                passwordInput.error = "Incorrect password"
            }
        }

        dialog.show()
    }


    private fun navigateToHome(user: User){
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("username", user.username)
        intent.putExtra("avaterResId", user.avatarResId)
        startActivity(intent)
    }
}