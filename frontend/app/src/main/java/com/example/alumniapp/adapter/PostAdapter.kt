package com.example.alumniapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alumniapp.R
import com.example.alumniapp.model.Post
// Import a library for image loading like Glide or Coil if you plan to load images from URLs
// For example, with Glide:
// import com.bumptech.glide.Glide

class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = posts.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userAvatar: ImageView = itemView.findViewById(R.id.imageViewUserAvatar)
        private val username: TextView = itemView.findViewById(R.id.textViewUsername)
        private val location: TextView = itemView.findViewById(R.id.textViewLocation)
        private val postImage: ImageView = itemView.findViewById(R.id.imageViewPostImage)
        private val description: TextView = itemView.findViewById(R.id.textViewDescription)
        // Add references for like, comment, share, options ImageViews if you need to set listeners or update them dynamically
        // private val likeButton: ImageView = itemView.findViewById(R.id.imageViewLike)

        fun bind(post: Post) {
            username.text = post.username
            location.text = post.location
            description.text = post.description

            // For image loading from URL, you would use a library here.
            // Example with a placeholder or local drawable:
            // if (post.avatarUrl != null) {
            // Glide.with(itemView.context).load(post.avatarUrl).circleCrop().into(userAvatar)
            // } else {
            // userAvatar.setImageResource(R.drawable.ic_default_avatar) // a default avatar
            // }
            // Glide.with(itemView.context).load(post.imageUrl).into(postImage)

            // Using placeholders for now
            // userAvatar.setImageResource(R.drawable.ic_avatar_placeholder) // Replace with actual placeholder
            // postImage.setImageResource(R.drawable.ic_image_placeholder) // Replace with actual placeholder
            
            // The XML uses tools:srcCompat for preview and hardcoded src/background for now.
            // To load dynamic images, you'll need an image loading library like Glide or Coil.
        }
    }
} 