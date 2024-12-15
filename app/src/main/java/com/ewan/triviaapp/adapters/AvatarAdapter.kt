package com.ewan.triviaapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.Avatar

class AvatarAdapter(private val avatars: List<Avatar>) :
    RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarImage: ImageView = itemView.findViewById(R.id.imgAvatar)
        val avatarName: TextView = itemView.findViewById(R.id.tvAvatarName)
        val avatarPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val buyButton: Button = itemView.findViewById(R.id.btnBuy)
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
        holder.avatarPrice.text = "200 💎"

        holder.buyButton.setOnClickListener {
            
        }
    }

    override fun getItemCount(): Int {
        return avatars.size
    }
}
