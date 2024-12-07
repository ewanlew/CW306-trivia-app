package com.ewan.triviaapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val username = intent.getStringExtra("username")
        val avatarResId = intent.getIntExtra("AvatarResId", R.drawable.avi_default)

    }

    override fun startActivity(intent: Intent){
        super.startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}