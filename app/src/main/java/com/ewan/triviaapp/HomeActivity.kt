package com.ewan.triviaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val username = intent.getStringExtra("username")
        val avatarResId = intent.getIntExtra("AvatarResId", R.drawable.avi_default)

        val startButton = findViewById<Button>(R.id.btnStart)
        startButton.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("avatarResId", avatarResId)
            startActivity(intent)
    }

    }
}