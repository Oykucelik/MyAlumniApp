package com.example.alumniapp.ui.mentorship

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alumniapp.data.model.Mentorship
import com.example.alumniapp.databinding.ItemMentorshipBinding
// Import Coil or Glide if you plan to load images, e.g.:
// import coil.load

class MentorshipAdapter(private val onItemClicked: (Mentorship) -> Unit) :
    ListAdapter<Mentorship, MentorshipAdapter.MentorshipViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentorshipViewHolder {
        val binding = ItemMentorshipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MentorshipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MentorshipViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }

    class MentorshipViewHolder(private val binding: ItemMentorshipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mentorship: Mentorship) {
            binding.mentorNameTextView.text = mentorship.mentorName
            binding.mentorshipTopicTextView.text = mentorship.topic
            binding.mentorshipStatusTextView.text = mentorship.status
            // Load image if URL is present, e.g., using Coil:
            // mentorship.mentorImageUrl?.let { url ->
            //     binding.mentorImageView.load(url) {
            //         crossfade(true)
            //         placeholder(R.drawable.ic_launcher_foreground) // Replace with your placeholder
            //     }
            // } ?: binding.mentorImageView.setImageResource(R.drawable.ic_launcher_foreground) // Default if no URL
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Mentorship>() {
            override fun areItemsTheSame(oldItem: Mentorship, newItem: Mentorship):
                Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Mentorship, newItem: Mentorship):
                Boolean = oldItem == newItem
        }
    }
} 