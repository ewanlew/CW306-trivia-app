package com.ewan.triviaapp.adapters

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.Avatar

class AvatarAdapter(
    private val avatars: List<Avatar>,
    private val context: Context,
    private val username: String
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("user_data", Context.MODE_PRIVATE)

    class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarImage: ImageView = itemView.findViewById(R.id.imgAvatar)
        val avatarName: TextView = itemView.findViewById(R.id.tvAvatarName)
        val avatarPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val actionButton: Button = itemView.findViewById(R.id.btnBuy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_avatar, parent, false)
        return AvatarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        val avatar = avatars[position]
        holder.avatarImage.setImageResource(avatar.imageResId)
        holder.avatarName.text = avatar.displayName

        val userUnlockedAvatars = getUnlockedAvatars()
        val userCurrentAvatar = sharedPref.getInt("$username:avatar", 0)

        // Check if avatar is unlocked
        if (userUnlockedAvatars.contains(avatar.imageResId)) {
            holder.avatarPrice.text = "" // Remove price for unlocked avatars
            holder.actionButton.text =
                if (avatar.imageResId == userCurrentAvatar) context.getString(R.string.inUse) else context.getString(R.string.equip)
        } else {
            holder.avatarPrice.text = context.getString(R.string.avatarPrice)
            holder.actionButton.text = context.getString(R.string.buy)
        }

        // Set button color
        when (holder.actionButton.text) {
            "In Use" -> holder.actionButton.setBackgroundColor(
                ContextCompat.getColor(context, R.color.gray)
            )
            "Equip" -> holder.actionButton.setBackgroundColor(
                ContextCompat.getColor(context, R.color.green)
            )
            "Buy" -> holder.actionButton.setBackgroundColor(
                ContextCompat.getColor(context, R.color.teal)
            )
        }

        // Handle button click actions
        holder.actionButton.setOnClickListener {
            when (holder.actionButton.text) {
                "Buy" -> handleBuyAction(avatar, holder)
                "Equip" -> handleEquipAction(avatar, holder)
            }
        }
    }

    override fun getItemCount(): Int {
        return avatars.size
    }

    private fun handleBuyAction(avatar: Avatar, holder: AvatarViewHolder) {
        val userGems = sharedPref.getInt("$username:currentGems", 0)
        if (userGems >= 200) {
            // Deduct gems
            updateGems(userGems - 200)

            // Unlock the avatar
            unlockAvatar(avatar.imageResId)

            // Update UI
            holder.actionButton.text = context.getString(R.string.equip)
            holder.avatarPrice.text = context.getString(R.string.avatarPrice)
            holder.actionButton.setBackgroundColor(
                ContextCompat.getColor(context, R.color.green) // Set to Equip color
            )
        } else {
            showToast(context.getString(R.string.notEnoughGems))
        }
    }

    private fun handleEquipAction(avatar: Avatar, holder: AvatarViewHolder) {
        // Update the currently equipped avatar
        val userCurrentAvatar = sharedPref.getInt("$username:avatar", 0)
        if (userCurrentAvatar != avatar.imageResId) {
            // Set the new avatar
            setCurrentAvatar(avatar.imageResId)

            // Notify the adapter to refresh all items
            notifyDataSetChanged()
        }
    }

    private fun getUnlockedAvatars(): MutableList<Int> {
        val unlockedAvatarsJson = sharedPref.getString("$username:unlockedAvatars", "[]")
        return unlockedAvatarsJson
            ?.removeSurrounding("[", "]")
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.toMutableList()
            ?: mutableListOf()
    }

    private fun unlockAvatar(avatarResId: Int) {
        val unlockedAvatars = getUnlockedAvatars()
        if (!unlockedAvatars.contains(avatarResId)) {
            unlockedAvatars.add(avatarResId)
            val editor = sharedPref.edit()
            editor.putString("$username:unlockedAvatars", unlockedAvatars.toString())
            editor.apply()
        }
    }

    private fun updateGems(newGems: Int) {
        val editor = sharedPref.edit()
        editor.putInt("$username:currentGems", newGems)
        editor.apply()
    }

    private fun setCurrentAvatar(avatarResId: Int) {
        val editor = sharedPref.edit()
        editor.putInt("$username:avatar", avatarResId)
        editor.apply()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
