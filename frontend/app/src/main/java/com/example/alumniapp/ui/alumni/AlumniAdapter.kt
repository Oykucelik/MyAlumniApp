package com.example.alumniapp.ui.alumni

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alumniapp.databinding.ItemAlumniBinding
import com.example.alumniapp.models.Alumni

class AlumniAdapter(private val onItemClick: (Alumni) -> Unit) : 
    ListAdapter<Alumni, AlumniAdapter.AlumniViewHolder>(AlumniDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlumniViewHolder {
        val binding = ItemAlumniBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlumniViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlumniViewHolder, position: Int) {
        val alumni = getItem(position)
        holder.bind(alumni)
    }

    inner class AlumniViewHolder(private val binding: ItemAlumniBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(alumni: Alumni) {
            binding.nameTextView.text = "${alumni.firstName} ${alumni.lastName}"
            
            // Load profile image if available
            // TODO: Implement image loading with a library like Glide or Picasso
            // For now, we're using a placeholder
        }
    }

    private class AlumniDiffCallback : DiffUtil.ItemCallback<Alumni>() {
        override fun areItemsTheSame(oldItem: Alumni, newItem: Alumni): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Alumni, newItem: Alumni): Boolean {
            return oldItem == newItem
        }
    }
} 