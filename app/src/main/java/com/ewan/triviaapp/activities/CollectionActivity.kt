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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitvity_collection)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAvatars)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch avatar items
        val avatarItems = fetchAvatars(this)

        // Set up adapter
        recyclerView.adapter = AvatarAdapter(avatarItems)
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

        if (avatarItems.isEmpty()) {
            Toast.makeText(this, "No avatars found!", Toast.LENGTH_SHORT).show()
        }

        return avatarItems
    }
}
