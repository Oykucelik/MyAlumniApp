package com.example.alumniapp.ui.jobs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alumniapp.data.model.Job
import com.example.alumniapp.databinding.ItemJobBinding

class JobAdapter(private val onApplyClicked: (Job) -> Unit, private val onItemClicked: (Job) -> Unit) : ListAdapter<Job, JobAdapter.JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }

    inner class JobViewHolder(private val binding: ItemJobBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val job = getItem(position)
                    onItemClicked(job)
                }
            }
        }
        fun bind(job: Job) {
            binding.jobTitle.text = job.title
            binding.jobCompany.text = job.company
            binding.jobLocation.text = job.location
            binding.jobDescription.text = job.description

            if (job.isApplied) {
                binding.applyButton.visibility = View.GONE
                binding.appliedBadge.visibility = View.VISIBLE
            } else {
                binding.applyButton.visibility = View.VISIBLE
                binding.appliedBadge.visibility = View.GONE
            }

            binding.applyButton.setOnClickListener {
                job.isApplied = !job.isApplied // Toggle state
                notifyItemChanged(adapterPosition) // Refresh the item view
                onApplyClicked(job) // Callback for any additional logic in fragment
            }
        }
    }

    class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            // Check isApplied state as well for content changes
            return oldItem == newItem && oldItem.isApplied == newItem.isApplied
        }
    }
} 