package com.example.alumniapp.ui.mentorship

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.alumniapp.R
import com.example.alumniapp.databinding.FragmentMentorshipBinding

class MentorshipFragment : Fragment() {

    private var _binding: FragmentMentorshipBinding? = null
    private val binding get() = _binding!!

    private val mentorshipViewModel: MentorshipViewModel by viewModels()
    private lateinit var mentorshipAdapter: MentorshipAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMentorshipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        setHasOptionsMenu(true)
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = getString(R.string.title_mentorships) // Make sure to add this string resource
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        mentorshipAdapter = MentorshipAdapter { mentorship ->
            // Navigate to detail screen
            val action = MentorshipFragmentDirections.actionMentorshipFragmentToMentorshipDetailFragment(mentorship)
            findNavController().navigate(action)
        }
        binding.mentorshipsRecyclerView.adapter = mentorshipAdapter
    }

    private fun observeViewModel() {
        mentorshipViewModel.mentorships.observe(viewLifecycleOwner) {
            mentorships -> mentorships?.let { mentorshipAdapter.submitList(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // Handle toolbar back button
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 