package com.ewan.triviaapp.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.adapters.AvatarAdapter
import com.ewan.triviaapp.models.Avatar

class CollectionActivity : AppCompatActivity() {

    private lateinit var username: String
    private lateinit var txtGems: TextView
    private lateinit var btnBuyHardcore: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitvity_collection)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        username = intent.getStringExtra("username") ?: ""

        txtGems = findViewById(R.id.tvGems)
        btnBuyHardcore = findViewById(R.id.btnBuyHardcore)

        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val gems = sharedPref.getInt("$username:currentGems", 0)
        val isHardcoreUnlocked = sharedPref.getBoolean("$username:hardcoreUnlocked", false)

        txtGems.text = getString(R.string.gemsShowcase, gems)

        if (isHardcoreUnlocked) {
            updateHardcoreButtonState(isUnlocked = true)
        } else {
            btnBuyHardcore.setOnClickListener {
                unlockHardcoreMode(sharedPref, gems)
            }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAvatars)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val avatarItems = fetchAvatars(this)
        recyclerView.adapter = AvatarAdapter(avatarItems, this, username, txtGems)
    }

    private fun unlockHardcoreMode(sharedPref: SharedPreferences, currentGems: Int) {
        if (currentGems >= 1000) {
            val newGemCount = currentGems - 1000
            val editor = sharedPref.edit()
            editor.putInt("$username:currentGems", newGemCount)
            editor.putBoolean("$username:hardcoreUnlocked", true)
            editor.apply()

            txtGems.text = getString(R.string.gemsShowcase, newGemCount)
            Toast.makeText(this, getString(R.string.unlockedHardcore), Toast.LENGTH_SHORT).show()
            updateHardcoreButtonState(isUnlocked = true)
        } else {
            Toast.makeText(this, getString(R.string.notEnoughGems), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHardcoreButtonState(isUnlocked: Boolean) {
        btnBuyHardcore.isEnabled = !isUnlocked
        btnBuyHardcore.text = if (isUnlocked) getString(R.string.unlocked) else getString(R.string.purchaseHardcore)
        btnBuyHardcore.setBackgroundColor(
            if (isUnlocked) getColor(R.color.gray) else getColor(R.color.teal)
        )
    }

    private fun fetchAvatars(context: Context): List<Avatar> {
        val avatarItems = mutableListOf<Avatar>()
        val drawableField = R.drawable::class.java.fields

        for (field in drawableField) {
            val name = field.name
            if (name.startsWith("avi_")) {
                val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
                val displayName = context.resources.getString(
                    context.resources.getIdentifier(name, "string", context.packageName)
                )
                avatarItems.add(Avatar(resId, displayName))
            }
        }
        return avatarItems
    }
}
