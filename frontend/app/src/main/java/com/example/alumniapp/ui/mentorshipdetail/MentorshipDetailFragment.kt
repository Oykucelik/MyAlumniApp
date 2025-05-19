package com.example.alumniapp.ui.mentorshipdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.alumniapp.R
import com.example.alumniapp.databinding.FragmentMentorshipDetailBinding
// Import Coil or Glide if you plan to load images
// import coil.load

class MentorshipDetailFragment : Fragment() {

    private var _binding: FragmentMentorshipDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MentorshipDetailViewModel by viewModels()
    private val args: MentorshipDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMentorshipDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        viewModel.setMentorship(args.mentorship)
        observeViewModel()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = getString(R.string.title_mentorship_detail)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        // Ensure options menu is re-created for this fragment
        setHasOptionsMenu(true) 
    }

    private fun observeViewModel() {
        viewModel.mentorship.observe(viewLifecycleOwner) { mentorship ->
            mentorship?.let {
                binding.detailMentorNameTextView.text = it.mentorName
                binding.detailMentorshipTopicTextView.text = it.topic
                binding.detailMentorshipStatusTextView.text = it.status
                binding.detailMentorshipDescriptionTextView.text = it.description
                binding.detailMentorshipDurationTextView.text = "${it.startDate} - ${it.endDate}"
                
                if (it.menteeName != null) {
                    binding.detailMenteeNameLabelTextView.visibility = View.VISIBLE
                    binding.detailMenteeNameTextView.visibility = View.VISIBLE
                    binding.detailMenteeNameTextView.text = it.menteeName
                } else {
                    binding.detailMenteeNameLabelTextView.visibility = View.GONE
                    binding.detailMenteeNameTextView.visibility = View.GONE
                }

                // Load image using Coil or Glide
                // it.mentorImageUrl?.let {
                //    binding.detailMentorImageView.load(it) {
                //        crossfade(true)
                //        placeholder(R.drawable.ic_launcher_foreground) // Replace with your placeholder
                //    }
                // } ?: binding.detailMentorImageView.setImageResource(R.drawable.ic_launcher_foreground)
            }
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