package com.example.alumniapp.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alumniapp.databinding.FragmentEventsBinding

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventsAdapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true) // For Up button
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        loadDummyEvents()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.eventsToolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Events"
    }

    private fun setupRecyclerView() {
        eventsAdapter = EventsAdapter()
        binding.eventsRecyclerView.apply {
            adapter = eventsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadDummyEvents() {
        val dummyEvents = listOf(
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
        eventsAdapter.submitList(dummyEvents)
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