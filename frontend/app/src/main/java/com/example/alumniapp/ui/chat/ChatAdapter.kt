package com.example.alumniapp.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alumniapp.data.model.Chat
import com.example.alumniapp.databinding.ItemChatBinding // Assuming this layout will be created

class ChatAdapter(private val onItemClicked: (Chat) -> Unit) :
    ListAdapter<Chat, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = getItem(position)
        holder.bind(chat)
        holder.itemView.setOnClickListener {
            onItemClicked(chat)
        }
    }

    class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.textViewUserName.text = chat.userName // Assuming a TextView with id textViewUserName
            binding.textViewLastMessage.text = chat.lastMessage // Assuming a TextView with id textViewLastMessage
            binding.textViewTimestamp.text = chat.timestamp // Assuming a TextView with id textViewTimestamp
            // Add image loading for profile picture if needed, e.g., using Glide or Picasso
            // if (chat.profileImageUrl != null) {
            // Glide.with(binding.imageViewProfile.context).load(chat.profileImageUrl).into(binding.imageViewProfile)
            // } else {
            // binding.imageViewProfile.setImageResource(R.drawable.ic_default_profile) // Placeholder
            // }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
} 