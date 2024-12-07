package com.ewan.triviaapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.User

class UserAdapter(
    private val userList: List<User>,
    private val onUserSelected: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val usernameText: TextView = view.findViewById(R.id.tvUsername)
        val avatarImage: ImageView = view.findViewById(R.id.imgAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.usernameText.text = user.username
        Log.d("UserAdapter", "Binding user: ${user.username} with Avatar Res ID: ${user.avatarResId}")

        // Set the image resource and log it
        holder.avatarImage.setImageResource(user.avatarResId)
        holder.avatarImage.post {
            Log.d(
                "UserAdapter",
                "ImageView dimensions (after bind) - Width: ${holder.avatarImage.width}, Height: ${holder.avatarImage.height}"
            )
        }

        holder.view.setOnClickListener { onUserSelected(user) }
    }



    override fun getItemCount(): Int = userList.size
}

