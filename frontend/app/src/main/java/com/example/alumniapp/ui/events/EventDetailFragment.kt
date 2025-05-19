package com.example.alumniapp.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.alumniapp.R
import com.example.alumniapp.databinding.FragmentEventDetailBinding
// import com.squareup.picasso.Picasso // If you want to load images

class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    private val args: EventDetailFragmentArgs by navArgs()

    // Temporary dummy data - in a real app, this would come from a ViewModel/Repository
    private val allEvents = listOf(
        Event(
            id = "1",
            title = "Annual Alumni Gala Dinner",
            date = "2024-08-15",
            time = "7:00 PM",
            location = "Grand Ballroom, Hilton Hotel",
            description = "Join us for a night of celebration, networking, and reconnecting with fellow alumni. Dress code: Formal."
        ),
        Event(
            id = "2",
            title = "Tech Talk: AI in Modern Industry",
            date = "2024-09-05",
            time = "2:00 PM - 4:00 PM",
            location = "Online (Zoom Webinar)",
            description = "A deep dive into how Artificial Intelligence is shaping various sectors. Guest speaker: Dr. Jane Doe, AI Specialist."
        ),
        Event(
            id = "3",
            title = "Homecoming Football Game & Tailgate",
            date = "2024-10-12",
            time = "12:00 PM onwards",
            location = "University Stadium & Grounds",
            description = "Cheer for our team and enjoy a festive tailgate party with food, games, and music. Family-friendly event!"
        ),
        Event(
            id = "4",
            title = "Startup Pitch Night",
            date = "2024-11-02",
            time = "6:00 PM",
            location = "Innovation Hub, 3rd Floor",
            description = "Watch aspiring entrepreneurs from our alumni community pitch their innovative startup ideas. Network with investors and innovators."
        ),
         Event(
            id = "5",
            title = "Alumni Holiday Mixer",
            date = "2024-12-10",
            time = "5:30 PM - 8:00 PM",
            location = "The Local Taphouse",
            description = "Get into the festive spirit with fellow alumni. Enjoy appetizers, drinks, and good company as we wrap up the year."
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true) // Important for handling options item selected for Up button
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        displayEventDetails()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.eventDetailToolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Event Details" // Or dynamically set based on event
        // You can also set the navigation icon click listener directly on the toolbar:
        // binding.eventDetailToolbar.setNavigationOnClickListener {
        // findNavController().navigateUp()
        // }
    }

    private fun displayEventDetails() {
        val eventId = args.eventId
        val event = allEvents.find { it.id == eventId }

        event?.let {
            // (activity as? AppCompatActivity)?.supportActionBar?.title = it.title // Set event title as toolbar title
            binding.eventDetailTitle.text = it.title
            binding.eventDetailDateTime.text = "${it.date} - ${it.time}"
            binding.eventDetailLocation.text = it.location
            binding.eventDetailDescription.text = it.description

            if (!it.imageUrl.isNullOrEmpty()) {
                binding.eventDetailImage.visibility = View.VISIBLE
                // Picasso.get().load(it.imageUrl).into(binding.eventDetailImage) // Uncomment if using Picasso
            } else {
                binding.eventDetailImage.visibility = View.GONE // Or set a placeholder
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 