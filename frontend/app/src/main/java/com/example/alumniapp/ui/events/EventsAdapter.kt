package com.example.alumniapp.ui.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alumniapp.R
import com.google.android.material.button.MaterialButton
// import com.squareup.picasso.Picasso // Add if you want to load images from URL

class EventsAdapter : ListAdapter<Event, EventsAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
        holder.itemView.setOnClickListener {
            val action = EventsFragmentDirections.actionEventsFragmentToEventDetailFragment(event.id)
            it.findNavController().navigate(action)
        }
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.event_title)
        private val dateTimeTextView: TextView = itemView.findViewById(R.id.event_date_time)
        private val locationTextView: TextView = itemView.findViewById(R.id.event_location)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.event_description)
        private val eventImageView: ImageView = itemView.findViewById(R.id.event_image)
        private val attendButton: MaterialButton = itemView.findViewById(R.id.attend_event_button)
        private val attendedBadgeTextView: TextView = itemView.findViewById(R.id.attended_badge_text)

        fun bind(event: Event) {
            titleTextView.text = event.title
            dateTimeTextView.text = "${event.date} - ${event.time}"
            locationTextView.text = event.location
            descriptionTextView.text = event.description

            if (!event.imageUrl.isNullOrEmpty()) {
                eventImageView.visibility = View.VISIBLE
                // Picasso.get().load(event.imageUrl).into(eventImageView) // Uncomment if using Picasso
                // For now, let's hide it if no dummy image or Picasso
                // If you have a placeholder drawable, you can set it here:
                // eventImageView.setImageResource(R.drawable.placeholder_event_image)
            } else {
                eventImageView.visibility = View.GONE
            }

            updateAttendanceUI(event)

            attendButton.setOnClickListener {
                if (event.isAttended) {
                    Toast.makeText(itemView.context, "Already attended '${event.title}'", Toast.LENGTH_SHORT).show()
                } else {
                    event.isAttended = true
                    Toast.makeText(itemView.context, "Successfully attending '${event.title}'", Toast.LENGTH_SHORT).show()
                    notifyItemChanged(adapterPosition)
                }
            }
        }

        private fun updateAttendanceUI(event: Event) {
            if (event.isAttended) {
                attendedBadgeTextView.visibility = View.VISIBLE
                attendButton.text = "Attended"
                attendButton.isEnabled = false
            } else {
                attendedBadgeTextView.visibility = View.GONE
                attendButton.text = "Attend"
                attendButton.isEnabled = true
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}