package com.ewan.triviaapp.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.adapters.AvatarAdapter
import com.ewan.triviaapp.models.Avatar

class CollectionActivity : AppCompatActivity() {

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitvity_collection)

        username = intent.getStringExtra("username") ?: ""

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAvatars)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val avatarItems = fetchAvatars(this)

        recyclerView.adapter = AvatarAdapter(avatarItems, this, username)
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
